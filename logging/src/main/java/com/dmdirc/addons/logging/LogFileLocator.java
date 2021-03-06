/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.logging;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.User;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Facilitates finding a path for log files.
 */
@Singleton
public class LogFileLocator {

    private static final Logger LOG = LoggerFactory.getLogger(LogFileLocator.class);
    private final Provider<String> directoryProvider;

    /** Whether to append a hash of the file name to the file name... */
    @ConfigBinding(key = "advanced.filenamehash")
    private boolean filenamehash;

    /** Whether to create a new folder for each network. */
    @ConfigBinding(key = "general.networkfolders")
    private boolean networkfolders;

    /** Whether to use date formats in file names. */
    @ConfigBinding(key = "advanced.usedate")
    private boolean usedate;

    /** Date format to use in file names if {@link #usedate} is true. */
    @ConfigBinding(key = "advanced.usedateformat")
    private String usedateformat;

    @Inject
    public LogFileLocator(
            @Directory(LoggingModule.LOGS_DIRECTORY) final Provider<String> directoryProvider,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(LoggingPlugin.class) final String domain) {
        this.directoryProvider = directoryProvider;

        globalConfig.getBinder().withDefaultDomain(domain).bind(this, LogFileLocator.class);
    }

    /**
     * Sanitises the log file directory.
     *
     * @return Log directory
     */
    private StringBuffer getLogDirectory() {
        final StringBuffer directory = new StringBuffer();
        directory.append(directoryProvider.get());
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory.append(File.separatorChar);
        }
        return directory;
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param channel Channel to get the name for
     *
     * @return the name of the log file to use for this object.
     */
    public String getLogFile(final GroupChat channel) {
        final StringBuffer directory = getLogDirectory();
        final StringBuffer file = new StringBuffer();
        final Optional<String> network = channel.getConnection().map(Connection::getNetwork);
        network.ifPresent(n -> addNetworkDir(directory, file, n));
        file.append(sanitise(channel.getName().toLowerCase()));
        return getPath(directory, file, channel.getName());
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param user Client to get the name for
     *
     * @return the name of the log file to use for this object.
     */
    public String getLogFile(final User user) {
        final StringBuffer directory = getLogDirectory();
        final StringBuffer file = new StringBuffer();
        addNetworkDir(directory, file, user.getConnection().getNetwork());
        file.append(sanitise(user.getNickname().toLowerCase()));
        return getPath(directory, file, user.getNickname());
    }

    /**
     * Gets the path for the given file and directory. Only intended to be used from getLogFile
     * methods.
     *
     * @param directory Log file directory
     * @param file      Log file path
     * @param md5String Log file object MD5 hash
     *
     * @return Name of the log file
     */
    public String getPath(final StringBuffer directory, final StringBuffer file,
            final String md5String) {
        if (usedate) {
            final String dateFormat = usedateformat;
            final String dateDir = new SimpleDateFormat(dateFormat).format(new Date());
            directory.append(dateDir);
            if (directory.charAt(directory.length() - 1) != File.separatorChar) {
                directory.append(File.separatorChar);
            }

            if (!new File(directory.toString()).exists()
                    && !new File(directory.toString()).mkdirs()) {
                LOG.info(USER_ERROR, "Unable to create data dirs");
            }
        }

        if (filenamehash) {
            file.append('.');
            file.append(md5(md5String));
        }
        file.append(".log");

        return directory + file.toString();
    }

    /**
     * This function adds the networkName to the log file. It first tries to create a directory for
     * each network, if that fails it will prepend the networkName to the filename instead.
     *
     * @param directory   Current directory name
     * @param file        Current file name
     * @param networkName Name of network
     */
    protected void addNetworkDir(final StringBuffer directory, final StringBuffer file,
            final String networkName) {
        if (!networkfolders) {
            return;
        }

        final String network = sanitise(networkName.toLowerCase());

        boolean prependNetwork = false;

        // Check dir exists
        final File dir = new File(directory + network + System.getProperty(
                "file.separator"));
        if (dir.exists() && !dir.isDirectory()) {
            LOG.info(USER_ERROR, "Unable to create networkfolders dir (file exists instead)");
            // Prepend network name to file instead.
            prependNetwork = true;
        } else if (!dir.exists() && !dir.mkdirs()) {
            LOG.info(USER_ERROR, "Unable to create networkfolders dir");
            prependNetwork = true;
        }

        if (prependNetwork) {
            file.insert(0, " -- ");
            file.insert(0, network);
        } else {
            directory.append(network);
            directory.append(System.getProperty("file.separator"));
        }
    }


    /**
     * Sanitise a string to be used as a filename.
     *
     * @param name String to sanitise
     *
     * @return Sanitised version of name that can be used as a filename.
     */
    protected static String sanitise(final String name) {
        // Replace illegal chars with
        return name.replaceAll("[^\\w\\.\\s\\-#&_]", "_");
    }

    /**
     * Get the md5 hash of a string.
     *
     * @param string String to hash
     *
     * @return md5 hash of given string
     */
    protected static String md5(final String string) {
        try {
            final MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(string.getBytes(), 0, string.length());
            return new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

}
