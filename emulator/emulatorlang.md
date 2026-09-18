# Basic layout

```
namespace [namespace];

[variables, functions, and classes]
```
*OR*

```
namespace [namespace] {

    [variables, functions, and classes]
}
```

# Importing
```
import [namespace] (as [alias]);
```

# Variables

## Namespace/class Variables

```
(<public|protected|private|internal>) (static) (<const|final>) [type] [name] (= [value]);
```

## Scope Variables
```
(<const|final>) [type] [name] (= [value]);
```

# Functions

## Static functions
```
(<public|protected|private|internal>) static (constexp) <void|[ret]> [name](...) {...}
(<public|protected|private|internal>) static extern <void|[ret]> [name](...);
```
## Instance functions
```
(<public|protected|private|internal>) (constexp) <void|[ret]> [name](...) {...}
(<public|protected|private|internal>) <abstract|extern> <void|[ret]> [name](...);

operator (constexp) [ret] [name](...) {...}
operator extern [ret] [name](...);
```
## Constructors/Deconstructors
```
(<public|protected|private|internal>) (~)[name](...) {...}
(<public|protected|private|internal>) extern (~)[name](...);
```

# Classes/Structs
```
struct [name] {...}

(abstract) class [name] {...}

extern class [name] <{...}|;>
```

# Visibilities
| Name        | Definition                                              |
|-------------|---------------------------------------------------------|
| `public`    | Anyone can access                                       |
| `protected` | Access is limited to the namespace and it's derivatives |
| `private`   | Access is limited to the namespace                      |
| `internal`  | Access is limited to the program unit                   |