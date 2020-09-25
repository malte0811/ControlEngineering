#### Bus wires (features and code structure)
 - Combine a fixed number of RS wires into a single wire
   - Different wire types
   - width: number of wires
   - bus line: index of wire
   - Width in range [1, MAX_WIDTH]
 - Connectors/relays do not have an inherent width
 - Wires of different width cannot be mixed on one connector
 - For connectors that can be configured depending on width
   - `getMinimumWidthForCurrentConfiguration` 
     - used to determine whether a wire can be attached, generally `max(used bus line)`
   - `getMaximumWidthForCurrentWires`
     - used to determine whether a bus line can be addressed, generally  
     `if wire attached: attached width else: MAX_WIDTH`
 - Local net handler analogous to RS local handler
   - Possibly with deterministic delay (updating values in handler tick)?  
   => Logic Boxes
 - Connectors
   - Relay
   - Line Access Connector
     - Extracts one RS wire
   - Bus Interface connector
     - Bus equivalent of Bundled RS connector from IE
   - Bus remapper?
     - Takes two bus wires, possibly of different width, allow arbitrary lines to be connected
 
#### Control panels (features)
 - Components
   - Mostly as in 1.12
 - Ideally get rid of panel connector blocks
   - Problem: Makes some designs infeasible
   - Possibly double-height version with dummy blocks?
 - Access to signals is only possible locally
   - Using Bus Interface Connector
 
#### Logic Boxes (features)
 - Mostly as described in gist from 2018
 - Circuit creation GUI ideas:
   - Traditional circuit layout UI
     - Complex to implement
     - Might be counter-intuitive (only some signals are valid input)
   - Topological order UI
     - Multiple columns of cells
     - Each column can only use outputs of previous columns as input
     - Might be a bit clunky
     - Bad representation of independent signal paths
   - Idea: Mix of traditional and topological
     - Cell placement on fine-ish grid
     - Grid is visible to user
     - Only allow connections to cells placed further to the right
     - Automatically enforces some degree of organization in layouts
 