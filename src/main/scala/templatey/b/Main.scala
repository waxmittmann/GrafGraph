package templatey.b

import scala.collection.mutable

/*
  Ok, versioning + everything:

  - we have an 'archetype' or 'class' or 'factory'
  - then we create instances; these are assigned an incremental id per instance
  - instances can be modified when created
  - instances can be migrated / versioned; this creates a new invisible instance with new version number; henceforth (from that
    migration forward) any reference to core instance can also be to any descendant.
    (to make simpler, could be just have a core reference, not be able to reference by version as well)
   so either:

   This one we will when building program have to create a copy of latest and store that inside new vertex.
   When migrate is called,
   ```
    klass Klass { defn }

    vertex initialFoo:Klass { defn }

    migrate initialFoo { defn } // creates initialFoo:2

    migrate backward-compatible initialFoo { defn } // not allowed to delete anything or add any mandatory fields

    vertex referencesInitial {
      ...
      edgeTo initialFoo // will be latest at this point
      edgeTo initialFoo:2 // will be a fixed version
      edgeTo initialFoo:latest // will be latest at this point
      edgeTo initialFoo:any // may be any version
      ...
    }
   ```
   or for second part just this, will allow any version.
   This one we will when building program mean that we build it up with mutable references, all pointing to a vertex,
   and just add versions to that vertex when migrate is called.

    ```
    vertex referencesInitial {
      ...
      edgeTo initialFoo // will be any version OR will be latest version
      ...
     }
    ```

    Now we can do version checking and return DAO case class for our graph objects that are versioned, like:
    ```
    case class InitialFoo.V1.Created(
      ...
    )
    case class InitialFoo.V1.Complete(
      ...
      status: "Complete"
      plus attributes ...
    )


    case class InitialFoo.V2.Created(
      ...
      plus edgeTo blah
    )
    case class InitialFoo.V2.Complete(
      ...
      status: "Complete"
      plus attributes ...
      plus edgeTo blah
    )


    // .get[InitialFoo.V2.Complete]("12e3er4-12e3-1223-122312")

    // 'Any' will be a version that has union of all fields of all non-retired versions, and any that may or may
    not exist will be Option (so anything that doesn't exist from first non-retired version)
    // .get[InitialFoo.Any.Complete]("12e3er4-12e3-1223-122312")

  states, versions, instances
    each instance is an instance of a klazz
    each instance has multiple versions (incl. initial)
      each version has multiple states (created, complete, ...)

    ```


    Could also:
    - retire versions (not gen source or allow)
    - create migration sets (sets of classes with a base version)

    Should also:
    - have behavior for failure to parse to compatible version (ignore, throw, log, ...)
    - some way of ensuring backward compatibility for minor versions, non-compatibility for major versions
    - have a migration action of migrating a node to a complex relationship, or vice versa (this will be hard)
        eg: WorkflowDefn->DockerImage TO WorkflowDefn->WorkflowStep->DockerImage

 */

object Main {

  /*
      The rule is to only model everything as outgoing connections (one posesses the other)?
      Then cycles make no sense? Conceptually this implies that a subelement can recursively divide itself
      if we provide a self recurse value (so an edge can go to itself) but not to an arbitrary ancestor.

      Which makes sense, because what would linking to an arbitrary ancestor look like? why would artefact x want to
      follow a link to anything that uses it? That seems bad!

      So, no backward links. Special self link. That means no circular dependencies. We get a DAG.
      That means we can build from case classes? Yes!



      class Vertex(
        val versions: Seq[VertexVersion]
        val latest: VertexVersion

        get(version: Version): VertexVersion
      )

      class VertexVersion(
        version: Version,
        allowedDefinitions: Seq[VertexDefinition]
      )

      class VertexDefinition(
        edges: Seq[Edge]
        attributes: Seq[Attribute]
      )

      class Vertex(
        edges: Seq[Edge]
      )

      sealed trait Edge
      case class OtherEdge(to: Vertex)
      case object SelfEdge
   */

  sealed trait Version
  sealed trait Attribute

  case class Vertex(
    versions: Seq[VertexVersion],
    latest: VertexVersion
  ) {
    def get(version: Version): VertexVersion = ???
  }

  case class VertexVersion(
    version: Version,
    allowedDefinitions: Seq[VertexDefinition]
  )

  case class VertexDefinition(
    edges: Seq[Edge],
    attributes: Seq[Attribute]
  )

//
//  class Vertex(
//    edges: Seq[Edge]
//  )

  sealed trait Edge
  case class OtherEdge(to: Vertex)
  case object SelfEdge

  def main(args: Array[String]): Unit = {

  }


}
