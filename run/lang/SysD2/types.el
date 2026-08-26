namespace SysD {
    struct AddressSpace {
        public int32 addressOffset;
        public int32 pid;
        public uint8 type;
        public uint8 state;
    }
    
    struct PeripheralDescriptorShort {
        const int32 deviceID;
        const int32 deviceType;
    }

    struct PeripheralDescriptor {
        const int32 id;
        const int32 type;
        const int32[4] manufacturer;
        const int32[4] serial;
        const int32[6] data;
    }

    extern const int32 REG_PGM_PNTR;
    extern const int32 REG_STACK_PNTR;
    extern const int32 REG_PID;
    extern const int32 REG_MEM_TABLE;
    extern const int32 REG_PRIVILEGED_MODE;
}