The **Research System** is a optional complementary system which allows players or server moderators to lock gameplay content behind a progression-tree of research nodes.

The Research Nodes system defines a 'research tree' of nodes, which can be unlocked in branching sequential fashion (either by a server command, or a code API calls). This progression, and in-game UI representation, is akin to Minecraft's Advancements system.

The entire system should be enabled (by default) or disabled from a boolean field in the server-config file.


### **Nodes**
Research Nodes are primarily used to do two things:
- Grant reward items
- Lock recipe validation

Each node is associated with the following data:
- A unique node ID
- A title
- A description
- An icon
- A list of prerequisite nodes
- A list of item rewards
- A list of recipe unlocks

When a research node is unlocked by a player, they will:
- Gain all reward items listed under the node, placed into their inventory or dropped as item entities
- Be flagged as having unlocked that node for the purposes of recipe validation

### Server Progression Architecture
Player progression along research trees are recorded in a server-storage data structure, keyed by node ID. Whenever a player unlocks a node, their UUID is added to the according entry.

This storage structure API is also responsible for recording recipe IDs which are locked behind research nodes, and exposing API methods for returning whether recipes are locked, unlocked, or setting lock states.

By toggling a 'global_unlocks' boolean in the server config, the Research Nodes system can also be placed into a 'global unlock' mode. In this mode, any unlock unlocks the node for all players on the server, and inserts a special 'all players' ID (something like "@a") to the node data entry.


### Recipe Implementation
Research Nodes are particularly orientated towards controlling recipe usage. When automated machines run their processing logic for crafting, those who wish to utilise the research system should integrate into the Research API and check whether a recipe is linked to a research node, and if so, whether the node is unlocked.

For standard API calls, a research-linked recipe is considered unlocked if at least one player on the server has unlocked the node.

If a research-linked recipe is locked, processing logic should abort and surface a helpful message to outputs such as the Goggle Tooltips.

**For v0.5 slice:** Only Resourceful Refinement mod content will hook into the the Research System API. Other Create blocks will be left unchanged.

##### **Player Crafting**
In addition to global machine unlocks as per above, research nodes also lock whether individual players are able to craft shaped or shapeless crafting recipes in their Inventory GUI or Crafting Table. This is a mixin implementation into vanilla Minecraft.

If a player has not unlocked a research node linked to a shaped or shapeless crafting recipe, they will not be able to craft the recipe in any form of crafting table. A warning label/message should instead appear below the output saying "Recipe Locked". This progression is recorded on a per-player basis.



### **Unlocking Nodes**
For initial v0.5 slice implementation, nodes will be granted and revoked via commands. Commands should permit the granting or revoking of individual nodes (even if their prerequisites are not met), and expose an optional 'cascade' flag, which when true, also grants all perquisite nodes to the target node as well or also revokes all dependent nodes linked to the target node.
