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
 - Creation: Using text-based format on paper tape
   - Concept: `BUTTON X 5 Y 3 COLOR FF00FF LATCHING YES\n`
   - CNC mill with integrated pick+place for actual creation
 
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

### 5 hole paper tape (features)
 - Two items with variable length: Punched paper tape and new paper tape (TODO names)
 - Punched tape can be read without machines
   - Concept: Left spool, right spool, tape with visible holes in between, desc above (color+on/off or char)
 - Can be glued together and cut
 - All holes: ignored in text mode
 - Creation:
   - Typewriter
     - Manual
     - Text based
     - automatically handles letter/number switches
     - Limited to 3? chars/second
     - On-screen keyboard, but still supports physical kb
     - Maybe: Also usable as TTY?
   - Transcriber (name TODO)
     - Copies book to paper tape
   - Manual puncher
     - Used to create "binary" tapes
     - Item with UI
     - Either direct binary or color+on/off
 - Sequencer
   - Accepts an RS interface connector
   - Reads tape one char at a time
   - 4 bits color, one bit on/off
   - Clock module (also used for logic boxes)
     - Determines when the tape advances/the LB steps
     - Swapped in-world, has its own UI as necessary
     - Edge-triggered
     - Types:
       - Free: Every `x` ticks
       - External
       - Gated: Every `x` ticks while RS input is high
   - Ideas:
     - Fast mode: three/four tapes, set every channel at once
     - Analog mode: two tapes, one for channel, one for strength
