#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 vertexColor;

uniform mat4 mvp; // projection * view * model
uniform vec4 color3D;
uniform float alpha;

out vec4 frag_color;
out float base_alpha;

void main()
{
    gl_Position = mvp * vec4(position, 1.0);
	gl_PointSize = 1; // if nearness increased size, automatic with triangles

    frag_color = color3D;
    base_alpha = alpha;
}
