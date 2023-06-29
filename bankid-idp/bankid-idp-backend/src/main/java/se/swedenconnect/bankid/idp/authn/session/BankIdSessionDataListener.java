package se.swedenconnect.bankid.idp.authn.session;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import se.swedenconnect.bankid.idp.authn.context.PreviousDeviceSelection;
import se.swedenconnect.bankid.idp.authn.events.*;
import se.swedenconnect.bankid.rpapi.types.CollectResponse;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class BankIdSessionDataListener {

  public static final Map<Boolean, PreviousDeviceSelection> PREVIOUS_DEVICE_SELECTION_MAP = Map.of(true, PreviousDeviceSelection.OTHER, false, PreviousDeviceSelection.THIS_DEVICE);
  private final BankIdSessionWriter writer;

  private final BankIdSessionReader reader;

  /**
   * Writes published OrderResponseEvent(s) to the users session
   * @see OrderResponseEvent
   * @param event to be processes
   */
  @EventListener
  public void handleOrderResponse(final OrderResponseEvent event) {
    log.info("Order response event was published {} for session {}", event.getResponse(), event.getRequest().getRequest().getSession().getId());
    final BankIdSessionData bankIdSessionData = BankIdSessionData.of(event.getRequest(), event.getResponse());
    writer.save(event.getRequest().getRequest(), bankIdSessionData);
  }

  /**
   * Writes published CollectResponseEvent(s) to the users session
   * @see CollectResponseEvent
   * @param event to be processed
   */
  @EventListener
  public void handleCollectResponse(final CollectResponseEvent event) {
    final HttpSession session = event.getRequest().getRequest().getSession();
    log.info("Collect response event was published {} for session {}", event.getCollectResponse(), session.getId());
    final BankIdSessionData previous = reader.loadSessionData(event.getRequest().getRequest()).getBankIdSessionData();
    writer.save(event.getRequest().getRequest(), BankIdSessionData.of(previous, event.getCollectResponse()));
    if (event.getCollectResponse().getStatus().equals(CollectResponse.Status.COMPLETE)) {
      writer.save(event.getRequest().getRequest(), event.getCollectResponse());
    }
  }

  /**
   * Writes published OrderCompletionEvent(s) to the users session
   * @see OrderCompletionEvent
   * @param event to be processed
   */
  @EventListener
  public void handleCompletion(final OrderCompletionEvent event) {
    final BankIdSessionState sessionState = reader.loadSessionData(event.getRequest());
    final Boolean otherDevice = sessionState.getBankIdSessionData().getShowQr();
    final PreviousDeviceSelection previousDeviceSelection = PREVIOUS_DEVICE_SELECTION_MAP.get(otherDevice);
    writer.save(event.getRequest(), previousDeviceSelection);
    writer.delete(event.getRequest());
  }

  /**
   * Handles published OrderCancellationEvent(s) to delete relevant user session data
   * @see OrderCancellationEvent
   * @param event to be processed
   */
  @EventListener
  public void handleOrderCancellationEvent(final OrderCancellationEvent event) {
    writer.delete(event.getRequest());
  }

  /**
   * Handles published UserVisibleDataEvent(s) to persist message for a user
   * @param event to be processed
   */
  @EventListener
  public void handleUserVisibleDataEvent(final UserVisibleDataEvent event) {
    writer.save(event.getRequest(), event.getUserVisibleData());
  }
}
