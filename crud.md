#Create
A -\> B* -\> C

1) wi -\> d, d -\> da, wi -\> wa


WorkflowDefn
Artifact

1) 
create definition (if required)
    create definition artifacts
create workflow instance
    link to workflow definiton
create workflow artifacts
    link to workflow artifact definitions

2) 
mark workflow instance as complete
mark artifacts as existing

WorkflowDefinition
ArtifactDefinition

WorkflowInstance.Running 
WorkflowInstance.Complete

WorkflowArtifact.Placeholder
WorkflowArtifact.Created

 

#Update


#Read


#Delete