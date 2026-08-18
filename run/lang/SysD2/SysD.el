namespace SysD {

    extern void memSet(uint32 address, uint32 value);
    extern void memSet(uint32 address, char value);

    extern uint32 memGet(uint32 address);

    extern void* sysCall(uint32 call);

    extern uint32 getPID();

    extern const uint32 MEMORY_DEVICE_START;
    extern const uint32 MEMORY_PROCESS_START;
    extern const uint32 MEMORY_BLOCK_SIZE;

    extern void copyFromReg(uint32 reg, uint32* target);
    extern void copyToReg(uint32* src, uint32 reg);
}