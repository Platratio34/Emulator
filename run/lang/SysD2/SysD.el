namespace SysD {

    extern void memSet(int32 address, int32 value);
    extern void memSet(int32 address, char value);

    extern int32 memGet(int32 address);

    extern void* sysCall(int32 call);

    extern int32 getPID();

    extern const int32 MEMORY_DEVICE_START;
    extern const int32 MEMORY_PROCESS_START;
    extern const int32 MEMORY_BLOCK_SIZE;

    extern void copyFromReg(int32 reg, int32* target);
    extern void copyToReg(int32* src, int32 reg);
}