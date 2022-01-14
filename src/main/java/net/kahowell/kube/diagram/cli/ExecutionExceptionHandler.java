package net.kahowell.kube.diagram.cli;

import picocli.CommandLine;

public class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception e, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
        String message = e.getLocalizedMessage();
        if (message == null) {
            throw e;
        }
        // TODO remove
        throw e;

        // bold red error message
//        commandLine.getErr().println(commandLine.getColorScheme().errorText(message));
//
//        return commandLine.getExitCodeExceptionMapper() != null
//                ? commandLine.getExitCodeExceptionMapper().getExitCode(e)
//                : commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}
