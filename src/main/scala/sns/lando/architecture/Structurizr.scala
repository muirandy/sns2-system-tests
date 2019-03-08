package sns.lando.architecture

import com.structurizr.Workspace
import com.structurizr.api.StructurizrClient
import com.structurizr.documentation.{Format, StructurizrDocumentationTemplate}
import com.structurizr.model.Tags
import com.structurizr.view.{PaperSize, Shape}

object Structurizr extends App {


  private val WORKSPACE_ID = 43191
  private val API_KEY = "fillMeIn"
  private val API_SECRET = "fillMeIn"

  // a Structurizr workspace is the wrapper for a software architecture model, views and documentation
  val workspace = new Workspace("SNS 2.0", "Event Driven Netstream on Kafka")
  val model = workspace.getModel
  // add some elements to your software architecture model
  val user = model.addPerson("SNS-North", "Call Centre")
  val softwareSystem = model.addSoftwareSystem("SNS 2.0", "My software system.")
  user.uses(softwareSystem, "Uses")
  // define some views (the diagrams you would like to see)
  val views = workspace.getViews
  val contextView = views.createSystemContextView(softwareSystem, "SystemContext", "An example of a System Context diagram.")
  contextView.setPaperSize(PaperSize.A5_Landscape)
  contextView.addAllSoftwareSystems()
  contextView.addAllPeople()
  // add some documentation
  val template = new StructurizrDocumentationTemplate(workspace)
  template.addContextSection(softwareSystem, Format.Markdown, "Here is some context about the software system...\n" + "\n" + "![](embed:SystemContext)")
  // add some styling
  val styles = views.getConfiguration.getStyles
  styles.addElementStyle(Tags.SOFTWARE_SYSTEM).background("#1168bd").color("#ffffff")
  styles.addElementStyle(Tags.PERSON).background("#08427b").color("#ffffff").shape(Shape.Person)
  val structurizrClient = new StructurizrClient (API_KEY, API_SECRET)
  structurizrClient.putWorkspace (WORKSPACE_ID, workspace)

  views.create
}