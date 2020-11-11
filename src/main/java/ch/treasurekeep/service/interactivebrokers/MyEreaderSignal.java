package ch.treasurekeep.service.interactivebrokers;

import com.ib.client.EReaderSignal;

/**
 * EReader signal of KOTT
 */
public class MyEreaderSignal implements EReaderSignal {
    @Override
    public void issueSignal() {
        // empty
    }
    @Override
    public void waitForSignal() {
        // empty
    }
}