# MapNet

> "Just as a wise leader knows that sometimes the best action is to do nothing, so too does this code demonstrate the power of non-action. Without any purpose or function, it exists in a state of effortless ease, free from the entanglements of attachment and expectation, uncoupled to any business logic."
>
> Lao Tse, <div style="text-align: right"> ... maybe </div>


![LaoTse](https://github.com/AdVerdu/map-net/blob/readme-assets/images/LaoTse-nonaction-meme.jpg)

This lib offers an extensible API for **Dynamic Chained Computations** defined as a YAML string **graph**.

# USE
The User Defined DSL should be placed in the **service layer** along with a YAML decoder for the type needed.
This way the features are decoupled from the definition improving maintainability and re-usability (features in the code / business logic in the YAML).

# Motivation
> When the US perimeter is changing faster than you can sprint.

This code follows the path of **Lao Tse** for the **non-action**. Allowing you to code only the features that are needed and letting you define/change the business logic in an Imperative way from a config file, avoiding unnecessary minors versions/hotfixes and speeding up CD.

(it's really just dependency injection with a lot of meme)

# Examples
## [Spark Implementation](https://github.com/AdVerdu/tao-spark)
