import SysD;

namespace Kernal {

    public static const char* CONSOLE_OUT = 0x1_0300;
    public static const char* CONSOLE_IN = 0x1_0301;
    public static const uint8* CONSOLE_IN_COUNT = 0x1_0302;

    @/
        Print a character to the terminal
    
        @param c The character to print
    /@
    public static inline void printChar(char c) {
        asm{
            STORE BYTE c Kernal.CONSOLE_OUT
        }
    }
    
    @/
        Print a null-terminated string to the terminal

        @param str Null-terminated character buffer
    /@
    public static void printStr(char* str) {
        asm{
            #alias r1 str
            SUB str r15 12
            LOAD MEM str str // str
            #alias r2 c
            :printStr_l1
                LOAD MEM BYTE c str INC_RA
                GOTO EQ c :printStr_l1_exit

                STORE BYTE c Kernal.CONSOLE_OUT
                GOTO :printStr_l1
            :printStr_l1_exit
        }
    }

    @/
        Write a string to the termanial.

        @param str The character buffer to write.
        @param len The number of characters to write
    /@
    public static void printStr(char* str, int32 len) {
        asm{
            #alias r14 len
            SUB len r15 12
            LOAD MEM len len
            #alias r1 consolePntr
            LOAD consolePntr Kernal.CONSOLE_OUT
            #alias r2 str
            SUB str r15 16
            LOAD MEM str str
            :printStr_len
                COPY MEM BYTE str consolePntr INC_RS
                INC len -1
                GOTO GT len :printStr_len
        }
    }

    @/
        Convets an int32 to hex string.

        @param value The value to convert
        @param str A character buffer of at least 8
    /@
    public static void intToHex(int32 value, char* str) {
        asm{
            LOAD r14 7
            SUB r1 r15 16
            LOAD MEM r1 r1 // value
            SUB r2 r15 12
            LOAD MEM r2 r2 // str
            LOAD r3 0xf
            :intToHex_l1
                LRT r1 r1 4
                AND r4 r1 r3
                SUB r5 r4 0xa
                GOTO GEQ r5 :intToHex_gt
                    INC r4 0x30
                    STORE BYTE r4 r2 INC_RA
                    GOTO :intToHex_l1_end
                :intToHex_gt
                    INC r4 0x57
                    STORE BYTE r4 r2 INC_RA
                :intToHex_l1_end
                INC r14 -1
                GOTO GEQ r14 :intToHex_l1
        }
    }
    
    @/
        Converts an int32 to decimal string
        
        @param value The value to convert
        @param str A character buffer, recomended to be at least 16 characters
    /@
    public static void intToDec(int32 value, char* str) {
        asm{
            SUB r1 r15 16
            LOAD MEM r1 r1 // value
            SUB r2 r15 12
            LOAD MEM r2 r2 // str
            COPY rStack r3
            #stackVar char[16] tempStr
            STACK INC 16 // char* str2
            LOAD r4 10
            LOAD r6 0x0
            :intToDec_l1
                DIV r1 r1 r4
                COPY rAF r7
                ADD r7 r7 0x30
                STORE BYTE r7 r3 INC_RA
                INC r6
                GOTO NEQ r1 :intToDec_l1
            INC r3 -1
            :intToDec_l2
                COPY MEM BYTE r3 r2 INC_RD
                INC r6 -1
                INC r3 -1
                GOTO NEQ r6 :intToDec_l2
            STORE BYTE 0x0 r2
            #stackVarClear tempStr
        }
    }

    @/
        Read the next up to `bufferSize` from the terminal input.
        If there are not enough characters to fill the buffer a `\0` will be placed after the last read character.
        
        @param buffer The character to read into
        @param bufferSize the maximum number of characters that can be read into the buffer.
    /@
    public static void read(char* buffer, int32 bufferSize) {
        asm{
            LOAD r1 Kernal.CONSOLE_IN_COUNT
            :read_l0
            LOAD MEM BYTE r2 r1
            GOTO EQ r2 :read_l0
        }
        int32 inCount = *CONSOLE_IN_COUNT;
        if(bufferSize < inCount) {
            inCount = bufferSize;
        }
        int32 i = 0;
        while(i < inCount) {
            buffer[i] = *CONSOLE_IN;
            // inCount--;
            i++;
        }
        if(i < bufferSize) {
            buffer[i] = '\0';
        }
    }
}