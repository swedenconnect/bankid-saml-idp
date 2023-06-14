package se.swedenconnect.bankid.idp.concurrency;

import java.util.concurrent.locks.Lock;

public interface LockRepository {
  Lock get(String key);
}
