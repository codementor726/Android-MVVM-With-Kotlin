If you don’t choose the right architecture for your Android project, you will have a hard time maintaining it as your codebase grows and your team expands.

This isn’t just an Android MVVM tutorial. In this article, we are going to combine MVVM (Model-View-ViewModel or sometimes stylized “the ViewModel pattern”) with Clean Architecture. We are going to see how this architecture can be used to write decoupled, testable, and maintainable code.

<b>Why MVVM with Clean Architecture?</b>

MVVM separates your view (i.e. Activitys and Fragments) from your business logic. MVVM is enough for small projects, but when your codebase becomes huge, your ViewModels start bloating. Separating responsibilities becomes hard.

MVVM with Clean Architecture is pretty good in such cases. It goes one step further in separating the responsibilities of your code base. It clearly abstracts the logic of the actions that can be performed in your app.

Note: You can combine Clean Architecture with the model-view-presenter (MVP) architecture as well. But since Android Architecture Components already provides a built-in ViewModel class, we are going with MVVM over MVP—no MVVM framework required!

<b>Advantages of Using Clean Architecture</b>

Your code is even more easily testable than with plain MVVM.
Your code is further decoupled (the biggest advantage.)
The package structure is even easier to navigate.
The project is even easier to maintain.
Your team can add new features even more quickly.
Disadvantages of Clean Architecture
It has a slightly steep learning curve. How all the layers work together may take some time to understand, especially if you are coming from patterns like simple MVVM or MVP.
It adds a lot of extra classes, so it’s not ideal for low-complexity projects.
Our data flow will look like this:

![Dataflow](https://uploads.toptal.io/blog/image/127608/toptal-blog-image-1543413671794-80993a19fea97477524763c908b50a7a.png)

The data flow of MVVM with Clean Architecture. Data flows from View to ViewModel to Domain to Data Repository, and then to a Data Source (Local or Remote.)
Our business logic is completely decoupled from our UI. It makes our code very easy to maintain and test.

The example we are going to see is quite simple. It allows users to create new posts and see a list of posts created by them. I’m not using any third-party library (like Dagger, RxJava, etc.) in this example for the sake of simplicity.

<b>The Layers of MVVM with Clean Architecture</b>

The code is divided into three separate layers:
Presentation Layer
Domain Layer
Data Layer

<b>MVVM with Clean Architecture: A Solid Combination</b>
Our purpose with this project was to understand MVVM with Clean Architecture, so we skipped over a few things that you can try to improve it further:

Use LiveData or RxJava to remove callbacks and make it a little neater.
Use states to represent your UI. (For that, check out this amazing talk by Jake Wharton.)
Use Dagger 2 to inject dependencies.
This is one of the best and most scalable architectures for Android apps. I hope you enjoyed this article, and I look forward to hearing how you’ve used this approach in your own apps!