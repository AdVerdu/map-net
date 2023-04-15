# MapNet

When there is a fixed business logic but the PO needs or the US perimeter is changing faster than you can sprint.

This code follows the path of Tao for the non-action. This code that does nothing, thus nothing couples with it (However, it's really able to do things).

This lib offers an extensible API for dynamic chained computations defined as a YAML string graph.

# USE
The User Defined DSL should be placed in the service layer along with a YAML decoder for the type needed.
This way the features are decoupled from the definition improving maintainability and re-usability.