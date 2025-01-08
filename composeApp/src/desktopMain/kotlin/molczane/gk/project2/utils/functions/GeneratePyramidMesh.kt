package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex

fun generatePyramidMesh(): Mesh {
    val baseCenter = Vector3(0f, 0f, -1f) // Center of the pyramid's base (partially below the surface)
    val height = 3f                      // Height of the pyramid
    val halfBaseSize = 2f                // Half the length of the base square's side

    // Define the pyramid's vertices
    val topVertex = Vertex(
        position = Vector3(0f, 0f, height),
        normal = Vector3(0f, 0f, 1f),
        uv = Vector3(0.5f, 0.5f, 0f) // UV coordinates for the top point
    )

    val baseVertices = listOf(
        Vertex(
            position = baseCenter + Vector3(-halfBaseSize, -halfBaseSize, 0f),
            normal = Vector3(0f, 0f, -1f), // Normal facing downward
            uv = Vector3(0f, 0f, 0f)       // UV for bottom-left
        ),
        Vertex(
            position = baseCenter + Vector3(halfBaseSize, -halfBaseSize, 0f),
            normal = Vector3(0f, 0f, -1f), // Normal facing downward
            uv = Vector3(1f, 0f, 0f)       // UV for bottom-right
        ),
        Vertex(
            position = baseCenter + Vector3(halfBaseSize, halfBaseSize, 0f),
            normal = Vector3(0f, 0f, -1f), // Normal facing downward
            uv = Vector3(1f, 1f, 0f)       // UV for top-right
        ),
        Vertex(
            position = baseCenter + Vector3(-halfBaseSize, halfBaseSize, 0f),
            normal = Vector3(0f, 0f, -1f), // Normal facing downward
            uv = Vector3(0f, 1f, 0f)       // UV for top-left
        )
    )

    // Define the base as two triangles
    val baseTriangles = listOf(
        Triangle(
            listOf(baseVertices[0], baseVertices[1], baseVertices[2])
        ),
        Triangle(
            listOf(baseVertices[0], baseVertices[2], baseVertices[3])
        )
    )

    // Define the walls as four triangles
    val wallTriangles = listOf(
        Triangle(
            listOf(
                baseVertices[0],
                baseVertices[1],
                topVertex.copy(normal = (baseVertices[1].position - topVertex.position).cross(baseVertices[0].position - topVertex.position).normalize())
            )
        ),
        Triangle(
            listOf(
                baseVertices[1],
                baseVertices[2],
                topVertex.copy(normal = (baseVertices[2].position - topVertex.position).cross(baseVertices[1].position - topVertex.position).normalize())
            )
        ),
        Triangle(
            listOf(
                baseVertices[2],
                baseVertices[3],
                topVertex.copy(normal = (baseVertices[3].position - topVertex.position).cross(baseVertices[2].position - topVertex.position).normalize())
            )
        ),
        Triangle(
            listOf(
                baseVertices[3],
                baseVertices[0],
                topVertex.copy(normal = (baseVertices[0].position - topVertex.position).cross(baseVertices[3].position - topVertex.position).normalize())
            )
        )
    )

    return Mesh(baseTriangles + wallTriangles)
}