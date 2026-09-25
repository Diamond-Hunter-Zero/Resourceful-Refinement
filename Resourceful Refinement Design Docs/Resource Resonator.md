#### **Description**
The **Resource Resonator** is a blockEntity tool, which allows players to scan their nearby area for world generation features, such as geysers, mineral deposits, and crystal fissures. the horizontal (X/Z) coordinates of these features are then displayed in a radar-style GUI.

**ID:** *resource_resonator*

#### **Behaviour**
The Resource Resonator is a horizontally directional block, may be placed down and then interacted with by right-clicking.

Right-clicking the resonator opens its GUI. This GUI consists of a circular radar-style screen, and a control-panel on the right where users can filter for which type of world-gen features they'd like to scan for, and press the 'Scan' button.

When a resonator performs a scan, it runs an analysis of all chunks within a configurable chunk distance (defined in server-config) to locate the spawn locations of all features matching the resonator's current filter. This scan is performed using the world seed to performantly assess chunks for features, and analyse chunks which may not have generated yet.

Once scanned, the results are locally cached in the resonator (these do not have to persist between sessions), and displayed on the radar screen as small icons. Each generation feature type has a distinct icon, and hovering over any POI on the radar screen displays it's block coordinates and type. This cache and UI is refreshed whenever a new scan is performed.


#### **Rendering**
The Resource Resonator uses a blockEntityRenderer and Java Entity Model for its visuals and animations.
