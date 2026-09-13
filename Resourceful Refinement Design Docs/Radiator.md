#### **Description**
Radiators are pipe-like blocks which heat or cool their surroundings. Depending on the fluid passing through them, they can act as a heat source for other Create machines.

**ID:** *radiator_pipe*

#### **Behaviour**
Radiators are pipe-like blocks which heat or cool their surroundings. They are fully directional blocks.

They function like pumps, having a front and back connecting fluid port, but are bi-directional. They don't pull or push fluids, or reset the pipe distance. They behave like standard Create pipes, with the exception that they do not connect to other pipes on their (local) side faces.

Most fluids which flow through a radiator won't change its heat. Those which do are partially consumed at a configurable rate as they flow through. This means a loop of radiators containing an effective fluid will slowly drain the fluid content.

Radiators have 3 heat states; "Cooled", "Chilled", and "Heated". These utilise the ExtendedHeatCondition enum. The example implementations are:
- If water is pumped through a radiator, it becomes 'Cooled'
- If Coolant is pumped through a radiator, it becomes 'Chilled'
- If Overcharged Carborax is pumped through a radiator it becomes 'Heated'

Cooled and Chilled radiators are used to cool Combustion Chambers or Distillery towers. Heated radiators can be used in place of Blaze Burners for any heating purpose.


#### **Rendering**
Radiators use standard java JSON block models for rendering. There are 4 different model JSONs used by the radiator, representing each of it's different heat states:
- *radiator_inert*
- *radiator_chilled*
- *radiator_cooled*
- *radiator_heated*