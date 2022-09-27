#version 150

in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float y = vertexColor.y;
    float x = vertexColor.x;
    float delay = 20 * vertexColor.z;

    float final_radius = 0.25;
    float current_radius = final_radius + (0.5 - final_radius) / (1. + delay);
    float relative_pos = 1. - clamp(abs(y - 0.5) / current_radius, 0., 1.);
    vec3 dot_color = vec3(0.75, 1, 1);
    vec3 trace_color = vec3(0.2, 1, 1);
    float decay = 1. - (current_radius - final_radius) / (0.5 - final_radius);
    fragColor = vec4(mix(dot_color, trace_color, decay), relative_pos * vertexColor.w);
}
