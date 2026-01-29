namespace SysD {
    struct AddressSpace {
        public uint32 addressOffset;
        public uint32 pid;
        public uint8 type;
        public uint8 state;
    }
    
    struct PeripheralDescriptor {
        const uint32 _ = 0x0;
        const uint32 type;
        const uint32[4] manufacturer;
        const uint32[4] serial;
        const uint32[6] reserved;
    }

    extern const uint32 REG_PGM_PNTR;
    extern const uint32 REG_STACK_PNTR;
    extern const uint32 REG_PID;
    extern const uint32 REG_MEM_TABLE;
    extern const uint32 REG_PRIVILEGED_MODE;
}