# Android native application

## Description
Descartaê is the easiest way to correctly discard your wastes. A project powered by the community and, Caravan Studios through Feito Na Biblioteca project.

## Apollo & Graphql
This project uses Graphql query language. So, you will need to constantly check for updates in your schema. 
In this project you can find the current schema in [android/app/src/main/graphql/org/descartae/android/schema.json](https://github.com/descartae/android/blob/develop/app/src/main/graphql/org/descartae/android/schema.json)

You can find instructions to setup apollo-codegen [HERE](https://github.com/apollographql/apollo-codegen).

Having apollo-codegen you just need to run the line above.

`apollo-codegen download-schema http://beta-api.descartae.com/graphql --output schema.json`

Compile your project to have Apollo generate the appropriate Java classes with nested classes and you are ready to go.

## How to run

**Android Studio**

- Import the project in android studio;
- Go to Run > Run app;
- Select a device, or create a new one by following the wizard;

**Feature request & Bug report**

You can report via (GitHub issues)[https://github.com/descartae/android/issues] o/
