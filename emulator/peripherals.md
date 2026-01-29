## Manager
Starts at `mem[0x8000]`
Rsp starts at `mem[0x8080]`
### Mapping
| Position | Description |
|----------|-------------|
| `0x8000` | Status (`0x0` pending, `0x1` ready for input) |
| `0x8001` | CMD status (`0x0` pending, `0x1` CMD written, `0x2` CMD read) |
| `0x8002` | Target Device (`0x0` for manager) |
| `0x8003` | CMD size |
| `0x8004` - `0x807f` | CMD |
| `0x8080` | RSP status (`0x0` pending, `0x1` RSP written, `0x2` RSP read, `0xf`) |
| `0x8081` | RSP Device |
| `0x8082` - `0x80ff` | RSP |

### Commands
| CMD | Arguments | Description | RSP |
|-----|-----------|-------------|-----|
| LIST `0x01` | Start ID | List all peripherals | `size deviceDescriptorShort[size] more` |
| Detail `0x02` | Device ID, rsp Pointer | Get the details on a peripheral | `` |

### Device descriptor
```
struct deviceDescriptorShort {
    uint32 deviceID;
    uint32 deviceType;
}

struct deviceDescriptor {
    uint32 deviceID;
    uint32 deviceType;
    ((packed))uint8[16] manufacture;
    ((packed))uint8[16] serial;
    uiunt32[6] usrData;
}
```

### Errors
| Code  | Description    |
|-------|----------------|
| `0x1` | No such device |

### Queue


## Console
TYPE: `0xff00_0001`

### Commands
| CMD | Arguments | Description | RSP |
|-----|-----------|-------------|-----|
| SETUP `0x01` | Queue start, Queue size | Setup the console's input queue | None |

## Storage
TYPE: `0x0100_0001`