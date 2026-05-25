Coating is a process in which a fluid and an item ingredient are applied onto a tool item, giving it a secondary durability bar, and a special temporary effect. Coating is a process performed by the Mechanical Forge Mould and Casting Depot on any tool item. It is a recipe type distinct from mechanical_forge_mould recipes.

#### **Application**
To Coat an item, the user places any tool onto a Casting Depot below a Mechanical Forge Mould. The Forge is then provided with an amount of fluid (specified by the recipe) and a secondary ingredient item. If a valid recipe is found, the forge performs it's processing animation, and 'coats' the tool.

A coated tool has a secondary durability bar, called it's 'Coating Integrity'. While a tool has any remaining coating integrity:
- Any damage that would be applied to the tool is instead applied to the coating integrity
- The user of the tool benefits from a 'coating effect' while holding the tool. This might be a status effect, an attribute modifier to the tool itself, or a logic method which runs when the tool is used.
- The coating integrity is rendered as a second durability bar above the first. It's colour corresponds to the effect type defined in the recipe.

Once a tool's coating is depleted, the coating and its effects are entirely removed from the tool, and the tool returns to regular behaviour.

New coatings cannot be applied to a tool if it already has a coating, unless the coating type matches the type produced by the current recipe. In this case, the coating integrity is simply reset to its full amount.

#### **Recipe Format**
Recipes for the coating process must define:
- A duration
- A fluid input
- An item input
- The coating type produced (Enum)

Each coating type defined must define:
- A name
- An associated colour
- A durability amount

