#version 330 core

out vec4 color;
in vec4 frag_color;
in float base_alpha;

void main()
{
    color = frag_color * vec4(1,1,1,base_alpha);
}
