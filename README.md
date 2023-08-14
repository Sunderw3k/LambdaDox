# LambdaDox

## Why?
Very often I see jars with lambdas not obfuscated, so I decided to create a jar of my own to exploit that and get a few real method names.   

## How?
When a method has a lamdba, the compiled class file adds a `private static synthetic` method called something like `lambda$main$0` where main is the method calling the lambda.  
  
The actual call to the lambda is instead replaced with an invokedynamic which calls the bootstrap method.   
For lambdas it is [LambdaMetaFactory.metafactory](https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/LambdaMetafactory.html#metafactory-java.lang.invoke.MethodHandles.Lookup-java.lang.String-java.lang.invoke.MethodType-java.lang.invoke.MethodType-java.lang.invoke.MethodHandle-java.lang.invoke.MethodType-). That bootstrap method takes a `MethodHandle` which contains the lambda name.

