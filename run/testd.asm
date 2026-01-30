:__start
INC rStack
COPY rStack r1
INC r1 -1
STORE rPgm r1

:__alt
INC rStack
COPY rPgm r1
COPY rStack r2
INC r2 -1
STORE r2 r1