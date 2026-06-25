
The Telemetry Terminal is a horizontally rotatable SmartEntityBlock, which connects to GLARE networks as a receiver, and enables network-wide communication through the telemetry system.


The Telemetry Terminal can be right-clicked to open a versatile GUI that allows player to view, send, and automate the handling of inbox messages. A row of browser-like tabs along the top of the GUI allow the user to switch the terminal between its 3 modes; MANUAL, AUTO-SEND, and AUTO-RECEIVE. Each mode represents a different function of the terminal, and a different GUI state or sub-screen.

Each Terminal can be assigned an Address by using a row of 3 Create item-slot behaviours positioned on the top face of the block (similar to the implementation in Redstone Links).

Where possible, reuse the existing confirm and trash button icons we have in our project textures

#### Manual
In its default manual mode, the Telemetry Terminal GUI behaves like an email program. It itself has two sub-screens it toggles between using tabs on the top-left; An 'Inbox' view, and a 'Compose  Message' view.

##### Inbox
The tab for the Inbox view says "Inbox (X messages)" when the address is storing 1 or more messages, or "Inbox" otherwise.

This view consists of a scroll-view along the left-hand edge showing a summary of each message currently in the inbox, with a top-row showing the sender's Address code as rendered items, and bottom row of grey text showing the first few characters of the message.

The right-hand two thirds of the view shows a large readout of the message content itself. When a message tab is selected in the left-side scrollview, it is displayed in full on the right hand side, with the address code along the top, and the sent datetime anchored to the top-right. The full text message is then printed below.
The method for rendering the text content over multiple lines should be defined in a generalized way that allows us to adjust the size and width of the right-hand-side panel when we come to making proper UI assets.
At the bottom-right of this content panel should be a button which says "Discard". Pressing this removes the message form the inbox.


##### Compose  Message
The Compose Message view allows the user to type a new message to send to an address.

On the left-hand edge of the view, is a scroll-view of all the terminal's 'saved' Addresses. Clicking on one will clear the current address and repopulate it with the saved one.

The rest of the view is taken up by the message editor:

Along the top, it has 3 item-slot buttons for setting the receiver's address code - Pressing these buttons opens a mini Creative-mode-style search window and search-box for finding any minecraft item (This should likely be it's own screen/class if possible). There is also a small 'Save' button to the right of this, which saves the current code as a saved contact if fully populated, and not already in the list.

The middle of the viewer is a large text field for the user to enter their messages body text. If possible, we should set this to warp text that goes over the panel's width. In the bottom-left corner of this is a 'Clear' button which clears all text

At the bottom of the view is a "Send" button. This button should only be enabled and function if the address is filled out, and the text body is non-empty. Pressing 'Send' sends the message to the target address if it exists on the network. If the address does not exist, the warning message along the bottom of the view will say "Address not found on this network". Otherwise, it briefly says "Message sent!", and resets the view.


#### Auto-Send
In auto send mode, the Telemetry Terminal GUI utilises an altered variant of the 'Compose  Message' view;

The address code and text body portions behave the same, allowing the user to set a new address and message.

In Auto-Send mode, there is no 'Send' button. Instead, whenever a user edits the the 'Compose  Message' view, the terminal caches this content and sends it to the address whenever it receives a redstone pulse. The bottom of the UI still shows the warning labels, which now displays either "Last message successfully sent" or "Last message unable to find address", depending on the success state of the most-recent operation.

Alternatively, if the Telemetry Terminal is the display target of one or more Create Display Links, the text body editor is disabled, and a header saying "Readout from Display Link" is instead shown, with the text produced by all attached display links shown below. In this mode, the combined content of the linked display links is sent as the text body on a redstone pulse (truncated if needed).


#### Auto-Receive
In auto receive mode, the Telemetry Terminal GUI has a unique view. It consists of a wide scroll-view which shows all current string-filters cached on the terminal. Each entry lists the string, and a small 'delete' button anchored to the far right (use the trash icon from our existing GUI assets) - Pressing this remove the string-filter from the terminal.

Below this scroll-view is a text input field, with a confirm button anchored to its right. Entering text into this field and pressing 'confirm' adds it as a new filter entry (if not already present), and clears the input field. 

Above the scroll view is a centred button which toggles between "Keep Messages" and "Discard Messages".

When an auto-receive terminal receives a message containing any of its string-filters, the terminal emits a redstone pulse for 1 tick (even if already emitting). If set to "Discard Messages", this then discards the triggering message from the inbox. String-filters, when set or when being compared to messages, should always be treated as case-invariant.

Auto-discarding received messages should be done in a way that any other auto-receive terminals listening to the same address will still trigger their 'comparison and emit' logic for that message that tick (i.e. We shouldn't immediately discard on processing, in-case other terminals also need to respond to this same message later during this tick).