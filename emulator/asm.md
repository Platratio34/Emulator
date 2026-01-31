| Code          | Instruction   | Description                                                                                     |
|---------------|---------------|-------------------------------------------------------------------------------------------------|
| `ff_ff_ff_ff` | `HALT`        | Stop execution of the process                                                                   |
| `00_xx_xx_xx` | `NO OP`       | Do nothing                                                                                      |
| `01_rg_00_00` | `LOAD`        | Load the next word into a register `r[rg]`                                                      |
| `01_rg_01_ra` | `LOAD MEM`    | Load `mem[ra]` to `r[rg]`                                                                       |
| `02_rs_00_rd` | `COPY`        | Store a value from `r[rs]` to `r[rd]`                                                           |
| `02_rg_01_ra` | `STORE`       | Store a value from `r[rg]` to `mem[r[ra]]`                                                      |
| `02_00_02_ra` | `STORE VAL`   | Store the next word to `mem[ra]`                                                                |
| `02_rs_03_rd` | `COPY MEM`    | Store a value from `mem[r[rs]]` to `mem[r[rd]]`                                                 |
| `04_op/rd_ra_rb` | MATH/LOGIC | Perform a math/logic operation on `r[ra]` and `r[rb]` storing the result into `r[rd]`, see spec |
| `05_op_ra_ro` | `GOTO`        | Goto command, see spec                                                                          |
| `10_rg_00_00` | `STACK PUSH`  | Push `r[rg]` to the stack                                                                       |
| `10_rg_01_00` | `STACK POP`   | Pop the top of the stack to `r[rg]`                                                             |
| `10_rg_02_xx` | `STACK INC`   | Increment the stac pointer by `xx` + 1                                                          |
| `10_rg_03_xx` | `STACK DEC`   | Decrement the stac pointer by `xx` + 1                                                          |
| `11_00_xx_xx` | `SYSCALL`     | Executes the System call `xx_xx`                                                                |
| `11_01_00_00` | `SYSRETURN`   | Returns from the current SysCall                                                                |
| `11_02_00_rg` | `SYSGOTO`     | Goto `r[rg]`, setting `rPM` to false                                                            |
| `11_03_op_rg` | `INTERRUPT`   | Interrupt instruction (see interrupt table)                                                     |


## Math/Logic operations
| Op Code | Description                 |
|---------|-----------------------------|
| `0x1`   | `r[ra] + r[rb] `            |
| `0x2`   | `r[ra] - r[rb] `            |
| `0x3`   | `r[rd] + 1`                 |
| `0x4`   | `r[ra] & r[rb] `            |
| `0x5`   | `r[ra] \| r[rb] `           |
| `0x6`   | `r[ra] NAND r[rb] `         |
| `0x7`   | `r[ra] NOR r[rb] `          |
| `0x8`   | `~r[ra]`                    |
| `0x9`   | `r[ra] ^ r[rb] `            |
| `0xa`   | Left shift `r[ra]` by `rb`  |
| `0xb`   | Right shift `r[ra]` by `rb` |
| `0xc`   | `r[ra] x r[rb]`             |
| `0xd`   | RESERVED                    |
| `0xe`   | RESERVED                    |
| `0xf`   | RESERVED                    |

## GOTO Operations
| Op Code | Description                                   |
|---------|-----------------------------------------------|
| `0x00`  | Unconditional GOTO `r[ra]`                    |
| `0x01`  | If `r[ro] == 0` GOTO `r[ra]`                  |
| `0x02`  | If `r[ro] <= 0` GOTO `r[ra]`                  |
| `0x03`  | If `r[ro] > 0` GOTO `r[ra]`                   |
| `0x04`  | If `r[ro] != 0` GOTO `r[ra]`                  |
|---------|-----------------------------------------------|
| `0x08`  | Unconditional GOTO `pPtr + ra`                |
| `0x09`  | If `r[ro] == 0` GOTO `pPtr + ra`              |
| `0x0a`  | If `r[ro] <= 0` GOTO `pPtr + ra`              |
| `0x0b`  | If `r[ro] > 0` GOTO `pPtr + ra`               |
| `0x0c`  | If `r[ro] != 0` GOTO `pPtr + ra`              |
|---------|-----------------------------------------------|
| `0x10`  | Unconditional GOTO `r[ra]`, push `pPtr`       |
| `0x11`  | If `r[ro] == 0` GOTO `r[ra]`, push `pPtr`     |
| `0x12`  | If `r[ro] <= 0` GOTO `r[ra]`, push `pPtr`     |
| `0x13`  | If `r[ro] > 0` GOTO `r[ra]`, push `pPtr`      |
| `0x14`  | If `r[ro] != 0` GOTO `r[ra]` , push `pPtr`    |
|---------|-----------------------------------------------|
| `0x18`  | Unconditional GOTO `pPtr + ra`, push `pPtr`   |
| `0x19`  | If `r[ro] == 0` GOTO `pPtr + ra`, push `pPtr` |
| `0x1a`  | If `r[ro] <= 0` GOTO `pPtr + ra`, push `pPtr` |
| `0x1b`  | If `r[ro] > 0` GOTO `pPtr + ra`, push `pPtr`  |
| `0x1c`  | If `r[ro] != 0` GOTO `pPtr + ra`, push `pPtr` |
|---------|-----------------------------------------------|
| `0x20`  | Unconditional GOTO `stack.pop`                |
| `0x21`  | If `r[ro] == 0` GOTO `stack.pop`              |
| `0x22`  | If `r[ro] <= 0` GOTO `stack.pop`              |
| `0x23`  | If `r[ro] > 0` GOTO `stack.pop`               |
| `0x24`  | If `r[ro] != 0` GOTO `stack.pop`              |

## INTERRUPT Options
| Op Code | Description                                                                |
|---------|----------------------------------------------------------------------------|
| `0x00`  | Trigger an interrupt using the value of `r[rg]` as the code                |
| `0x01`  | Trigger an interrupt using the next word as the code                       |
| `0xff`  | Return from the interrupt, popping r1-15, rPM, and then rPgm off the stack |

# Instructions

| Name                                                      | Pv  | Description                                              | Notes |
|-----------------------------------------------------------|-----|----------------------------------------------------------|-------|
| `HALT`                                                    | Yes | Halt the execution of the program                        |       |
| `LOAD [rg] [value]`                                       |     | Load `value` into `r[rg]`                                | 1     |
| `LOAD MEM [rg] [ra]`                                      |     | Load `mem[r[ra]]` into `r[rg]`                           |       |
| `COPY [rs] [rd]`                                          |     | Copy `r[rs]` to `r[rd]`                                  |       |
| `COPY MEM [rs] [rd]`                                      |     | Copy `mem[r[rs]]` to `mem[r[rd]]`                        |       |
| `STORE [rg] [ra]`                                         |     | Store the value from `r[rg]` to `mem[r[ra]]`             |       |
| `STORE VAL [value] [ra]`                                  |     | Store `value` to `mem[r[ra]]`                            | 1     |
| `STORE [value] [ra]`                                      |     | Store `value` to `mem[r[ra]]`                            | 1     |
| `ADD [rd] [ra] [rb]`                                      |     | Add `r[ra]` and `r[rb]` and store it into `r[rd]`        | 2     |
| `SUB [rd] [ra] [rb]`                                      |     | Subtract `r[ra]` and `r[rb]` and store it into `r[rd]`   | 2     |
| `INC [rd] ([value])`                                      |     | Increment `r[rd]` (increments by `value` if present)     | 2, 3  |
| `MUL [rd] [ra] [rb]`                                      |     | Multiplies `r[ra]` and `r[rb]` and store it into `r[rd]` | 2     |
| `GOTO (<PUSH\|POP>) (<EQ\|LEQ\|GT\|NEQ>) ([rg]) [:label]` |     | Goto label statement (See detail section)                |       |
| `GOTO (<PUSH\|POP>) (<EQ\|LEQ\|GT\|NEQ>) ([rg]) [ra]`     |     | Goto absolute statement (See detail section)             |       |
| `STACK <PUSH\|POP> [rg]`                                  |     | Push/Pop `r[rg]` to/from the stack                       |       |
| `STACK <INC\|DEC> ([val])`                                |     | Inc/dec `rStack` by value (def 1)                        |       |
| `SYSCALL [function]`                                      |     | Perform a system call. (`function` may be index or name) |       |
| `SYSRETURN`                                               | Yes | Return from a system call                                |       |
| `SYSGOTO [rg]`                                            | Yes | Goto `r[rg]`, setting `rPM` to false                     |       |
| `INTERRUPT [code]`                                        |     | Trigger an interrupt with code `code`                    | 1     |
| `INTERRUPT [rg]`                                          |     | Trigger an interrupt with code `r[rg]`                   |       |
| `INTERRUPT RET`                                           | Yes | Return from the interrupt                                |       |

^1. Uses 2 words in memory

^2. `rd` Must be in the range [0, 15]

^3. `value` must be in the range `[-32,767, 0)` or `(0, 32,767]`

## Compiler directives

### `:[label]`
Defines a GOTO label

### `#define [name] [value]`
Defines a compiler alias for the given value.

If the value is a string, the value will be a pointer to the string in the program memory

### `#function [name]`
Defines a function. Also defines a GOTO label with the function name

If the function name starts with `syscall::` it is tracked for syscall assignment

### `#syscall [index] [function]`
Map a syscall index to specified function name (`syscall::[function]`)