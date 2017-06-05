.class public Hello
.super java/lang/Object
.field static b I = 2

.method public static main([Ljava/lang/String;)V
.limit stack 4
.limit locals 5
bipush 11
newarray int
astore_0
aload_0
arraylength
iconst_1
isub
dup
initLoop0:
iflt endInit0
dup
aload_0
swap
bipush 10
iastore
iconst_1
isub
dup
goto initLoop0
endInit0:
pop
iconst_3
istore_1
bipush 10
istore_2
loop0:
iload_2
iload_1
isub
iflt label0
aload_0
iload_2
iaload
iload_2
imul
istore_4
iinc 2 -1
goto loop0
label0:
iconst_1
newarray int
astore_3
return
.end method
.method public static f1()[I
.limit stack 4
iconst_3
newarray int
astore_0
aload_0
arraylength
iconst_1
isub
dup
initloop0:
iflt endInit0
dup
aload_0
swap
iconst_2
iastore
iconst_1
isub
dup
goto initloop0
endInit0:
pop
aload 0
areturn
.end method

