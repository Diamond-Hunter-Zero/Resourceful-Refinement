#### **Description**
The Lux Transceiver is a directional GLARE network block which connects to GLARE networks as a receiver node, and provides Lux to any blocks with a Lux-Socket interface connected to its back face.

**ID:** *lux_transceiver*
**Vertical Slice:** 0.4

#### **Behaviour**
The Lux Transceiver is a directional block, which can face in any of the 6 directions. Like other GLARE nodes, it can be connected to a network, and supports a single link. The Lux Transceiver does not consume any Lux itself.

BlockEntities can expose a LuxSocket interface on their faces, much like Item and Fluid Handlers. The socket points to a GLARE interface implementation on the blockEntity (or a controller); When a Lux Transceiver is placed such that it's local back face connects to a socket, and the transceiver is connected to a network, the transceiver forms a link with the blockEntity's GLARE node. This link is special, in that it does not care about LoS (and will not participate in such checks), will not render any beams, and does not contribute to any link limits or counts on the transceiver or blockEntity. The socket interface (and the blockEntity/controller) itself is not a GLARE node, and cannot be targeted by other GLARE nodes or by the connection tool.

When either the transceiver or blockEntity are moved/removed/rotated, the link may break as per normal. If the socket interface is ever disabled or removed, the link also breaks as expected.

If a blockEntity (or controller) has multiple sockets connected to functioning transceivers, the transceivers' networks merge as they would with any other relay chain. Attaching multiple transceivers to a blockEntity does not multiply the amount of Lux consumed, as only the blockEntity is actually being allocated LUX. It merely allows more connected networks to be build.

#### **Rendering**
The Lux Transceiver uses a BlockEntityRenderer to render its entityModel visuals.