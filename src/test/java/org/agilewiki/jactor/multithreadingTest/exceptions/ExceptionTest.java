package org.agilewiki.jactor.multithreadingTest.exceptions;

import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.bind.Open;
import org.agilewiki.jactor.components.Include;
import org.agilewiki.jactor.components.JCActor;
import org.junit.Test;

/**
 * Test code.
 */
public class ExceptionTest {
    @Test
    public void test() {
        JAMailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(10);
        try {
            JCActor a = new JCActor(mailboxFactory.createMailbox());
            (new Include(Divider.class)).call(a);
            Open.req.call(a);
            JAFuture future = new JAFuture();

            try {
                (new SyncDivide(3, 0)).send(future, a);
            } catch (Exception x) {
                System.out.println("test 1 => " + x.toString());
            }

            try {
                (new Divide(3, 0)).send(future, a);
            } catch (Exception x) {
                System.out.println("test 2 => " + x.toString());
            }

            System.out.println("test 3 => " + (new ISyncDivide(3, 0)).send(future, a));

            System.out.println("test 4 => " + (new IDivide(3, 0)).send(future, a));

            try {
                (new Divide(3, 0)).send(future, a);
            } catch (Exception x) {
                System.out.println("test 5 => " + x.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}
