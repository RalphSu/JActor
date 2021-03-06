package org.agilewiki.jactor.components.pubsub;

import junit.framework.TestCase;
import org.agilewiki.jactor.*;
import org.agilewiki.jactor.bind.Open;
import org.agilewiki.jactor.components.Include;
import org.agilewiki.jactor.components.JCActor;

/**
 * Test code.
 */
public class PubSubTest extends TestCase {
    public void test() {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        try {
            Mailbox mailbox = mailboxFactory.createMailbox();
            JAFuture future = new JAFuture();
            JCActor publisher = new JCActor(mailbox);
            (new Include(PubSub.class)).call(publisher);
            Open.req.call(publisher);
            Actor subscriber1 = new Subscriber(mailbox);
            Open.req.call(subscriber1);
            Actor subscriber2 = new Subscriber(mailbox);
            Open.req.call(subscriber2);
            (new Subscribe(subscriber1)).call(publisher);
            (new Subscribe(subscriber2)).call(publisher);
            (new Publish(new PSRequest())).send(future, publisher);
            (new Unsubscribe(subscriber1)).call(publisher);
            (new Publish(new PSRequest())).send(future, publisher);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}
