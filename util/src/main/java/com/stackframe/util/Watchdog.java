package com.stackframe.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

/**
 * A tool for watching over operations that might be taking longer than they should.
 */
public class Watchdog {

    private static final AlertAction defaultAlertAction = new AlertAction() {
        @Override
        public void operationTookTooLong(Throwable creationStack) {
            System.err.println("Operation took too long. Stack at creation time=");
            creationStack.printStackTrace(System.err);
        }
    };

    private Collection<Stack<Token>> stacks = Collections.synchronizedList(new LinkedList<>());
    private final Thread watchdogThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {
                for (Stack<Token> stack : stacks) {
                    if (!stack.isEmpty()) {
                        Token token = stack.peek();
                        long now = System.currentTimeMillis();
                        if (now > token.expectedEndTime) {
                            token.alertAction.operationTookTooLong(token.creationStack);
                        }
                    }
                }

                long wakeupTime = soonestWakeup();
                long now = System.currentTimeMillis();
                try {
                    Thread.sleep(wakeupTime - now);
                } catch (InterruptedException e) {
                    // Do nothing. We expect to be interrupted. We just proceed into the loop again.
                }
            }
        }

        private long soonestWakeup() {
            long soonestWakeup = Long.MAX_VALUE;
            for (Stack<Token> stack : stacks) {
                if (!stack.isEmpty()) {
                    Token token = stack.peek();
                    soonestWakeup = Long.min(token.expectedEndTime, soonestWakeup);
                }
            }

            return soonestWakeup;
        }
    });

    private final ThreadLocal<Stack<Token>> operationStack = new ThreadLocal<Stack<Token>>() {

        @Override
        public void remove() {
            stacks.remove(get());
            super.remove();
        }

        @Override
        protected Stack<Token> initialValue() {
            return new Stack<>();
        }

    };

    public Watchdog() {
        watchdogThread.setName("Watchdog");
        watchdogThread.setPriority(Thread.MAX_PRIORITY);
        watchdogThread.start();
    }

    /**
     * Start an operation.
     *
     * @param expectedDuration the expected duration of the operation, in milliseconds
     * @param alertAction the action to take when the operation takes too long
     * @return a token representing the operation
     */
    public Token start(long expectedDuration, AlertAction alertAction) {
        Stack<Token> stack = operationStack.get();
        long now = System.currentTimeMillis();
        Token token = stack.push(new Token(now + expectedDuration, alertAction));
        watchdogThread.interrupt();
        return token;
    }

    /**
     * Start an operation with the default alert action.
     *
     * @param expectedDuration the expected duration of the operation, in milliseconds
     * @return a token representing the operation
     */
    public Token start(long expectedDuration) {
        return start(expectedDuration, defaultAlertAction);
    }

    /**
     * Stop an operation that the watchdog is watching.
     *
     * @param token the Token returned by a previous call to start()
     */
    public void stop(Token token) {
        Stack<Token> stack = operationStack.get();
        Token found = stack.pop();
        if (found != token) {
            throw new IllegalStateException("operation not at top of stack");
        }
    }

    /**
     * Define an implementation of this interface if you want to customize what happens when the watchdog finds an operation that
     * has taken too long.
     */
    public interface AlertAction {

        /**
         * This method is called when an operation has taken too long.
         *
         * @param creationStack a Throwable captured when the operation is started. This is useful for determining what caused the
         * operation.
         */
        void operationTookTooLong(Throwable creationStack);
    }

    public class Token {

        private final long expectedEndTime;
        private final AlertAction alertAction;
        private final Throwable creationStack = new Throwable();

        private Token(long expectedEndTime, AlertAction alertAction) {
            this.expectedEndTime = expectedEndTime;
            this.alertAction = alertAction;
        }
    }
}
