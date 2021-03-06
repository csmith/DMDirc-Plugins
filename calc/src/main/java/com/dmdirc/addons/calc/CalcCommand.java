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

package com.dmdirc.addons.calc;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.text.ParseException;

/**
 * A command which allows users to evaluate various mathematical expressions, and perform basic
 * calculations.
 */
public class CalcCommand extends BaseCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("calc",
            "calc [--showexpr] <expr> - evaluate mathematical expression",
            CommandType.TYPE_GLOBAL);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public CalcCommand(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        try {
            int offset = 0;
            boolean showexpr = false;

            if (args.getArguments().length > 0 && "--showexpr".equals(args.getArguments()[0])) {
                showexpr = true;
                offset++;
            }

            final String input = args.getArgumentsAsString(offset);
            final Lexer lexer = new Lexer(input);
            final Parser parser = new Parser(lexer);
            final Evaluator evaluator = new Evaluator(parser.parse());
            final Number result = evaluator.evaluate();
            showOutput(origin, args.isSilent(), (showexpr ? input + " = " : "") + result);
        } catch (ParseException ex) {
            showError(origin, args.isSilent(), "Unable to parse expression: " + ex.getMessage());
        } catch (ArithmeticException ex) {
            showError(origin, args.isSilent(), "Unable to calculate expression: " + ex.getMessage());
        }
    }

}
