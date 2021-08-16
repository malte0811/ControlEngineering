Working title: Control engineering 
 - This is an [existing term](https://en.wikipedia.org/wiki/Control_engineering) for something that roughly matches the goals of the addon
#### Bus wires
 - Combine 4 RS wires into one
   - Having only 16 signals has been a limiting factor in 1.12
 - Local net handler analogous to RS local handler
   - Possibly with deterministic delay (updating values in handler tick)?  
   => Logic Boxes
 - Connectors
   - Relay
   - Line Access Connector
     - Extracts one RS wire
   - Bus Interface connector
     - Bus equivalent of Bundled RS connector from IE
 
#### Control panels
 - Components
   - Mostly as in 1.12
 - Ideally get rid of panel connector blocks
   - Problem: Makes some designs infeasible
   - Possibly double-height version with dummy blocks?
 - Access to signals is only possible locally
   - Using Bus Interface Connector
 - Creation: Using text-based format on paper tape
   - Concept: `BUTTON X 5 Y 3 COLOR FF00FF LATCHING YES;`
   - CNC mill with integrated pick+place for actual creation
   - Solves the problem of creating multiple of the same panel (hard to impossible in 1.12)
   - Probably will need to implement a nice UI for it later on...
   - CNC creates panel "surface", panel chassis is created separately
 
#### Logic Boxes
 - Mostly as described in [gist from 2018](https://gist.github.com/malte0811/c1ad8a86764bd3216b253200cedee7af)
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

### ~~5~~ 8 hole paper tape
 - Two items with variable length: Punched paper tape and new paper tape (TODO names)
 - Punched tape can be read without machines
   - Concept: Left spool, right spool, tape with visible holes in between, desc above (color+intensity or char)
 - Can be glued together and cut
 - All holes: ignored in text mode
 - Creation:
   - Typewriter
     - Manual
     - Text based
     - Limited to 3? chars/second
     - On-screen keyboard, but still supports physical kb
     - Maybe: Also usable as TTY?
   - Transcriber (name TODO)
     - Copies book to paper tape
   - Manual puncher
     - Used to create "binary" tapes
     - Item with UI
     - Either direct binary or color+intensity
 - Sequencer
   - Accepts an RS interface connector
   - Reads tape one char at a time
   - 4 bits color, 4 bits intensity
   - Clock module (also used for logic boxes)
     - Determines when the tape advances/the LB steps
     - Swapped in-world, has its own UI as necessary
     - Edge-triggered
     - Types:
       - Free: Every `x` ticks
       - External
       - Gated: Every `x` ticks while RS input is high
   - Ideas:
     - Fast mode: two tapes, set every channel to binary value at once
