package org.agilewiki.jactor.stateMachine;

import junit.framework.TestCase;
import org.agilewiki.jactor.*;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Test code.
 */
public class SendVVTest extends TestCase {
    public void test() {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        try {
            Actor actor = new Send(mailboxFactory.createMailbox());
            JAFuture future = new JAFuture();
            System.out.println(future.send(actor, null));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }

    class Doubler extends JLPCActor {

        Doubler(Mailbox mailbox) {
            super(mailbox);
        }

        @Override
        public void processRequest(Object request, RP rp)
                throws Exception {
            int req = (Integer) request;
            rp.processResponse(req * 2);
        }
    }

    class Send extends JLPCActor {

        Send(Mailbox mailbox) {
            super(mailbox);
        }

        @Override
        public void processRequest(Object request, RP rp)
                throws Exception {
            Doubler doubler = new Doubler(getMailbox());
            SMBuilder smb = new SMBuilder();
            smb._send(doubler, 21, "rsp");
            smb._return(new ObjectFunc() {
                @Override
                public Object get(StateMachine sm) {
                    return sm.get("rsp");
                }
            });
            smb.call(rp);
            //Output:
            //42
        }
    }
}
