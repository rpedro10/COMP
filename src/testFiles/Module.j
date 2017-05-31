.class public Hello
.super java/lang/Object
.field static a I
.field static b I = 10
.field static c [I
.field static d [I

.method public static main([Ljava/lang/String;)V
ldc "Hello World"
invokestatic io/print(Ljava/lang/String;)V
return
.end method

.method static public <clinit>()V
.limit stack 2
.limit locals 0
bipush 10
newarray int
putstatic Hello/d [I
return
.end method
