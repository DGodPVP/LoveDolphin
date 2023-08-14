////////////////////////////////////////
// Complementary Reimagined by EminGT //
////////////////////////////////////////

//Common//
#include "/lib/common.glsl"

//////////Fragment Shader//////////Fragment Shader//////////Fragment Shader//////////
#ifdef FRAGMENT_SHADER

noperspective in vec2 texCoord;

//Uniforms//
uniform float viewWidth, viewHeight;
uniform float far, near;

uniform vec3 cameraPosition, previousCameraPosition;

uniform mat4 gbufferPreviousProjection, gbufferProjectionInverse;
uniform mat4 gbufferPreviousModelView, gbufferModelViewInverse;

uniform sampler2D colortex3;
uniform sampler2D colortex2;
uniform sampler2D colortex1;
uniform sampler2D depthtex1;

//Pipeline Constants//
#include "/lib/pipelineSettings.glsl"

const bool colortex3MipmapEnabled = true;

//Common Variables//

//Common Functions//
float GetLinearDepth(float depth) {
    return (2.0 * near) / (far + near - depth * (far - near));
}

//Includes//
#ifdef TAA
	#include "/lib/antialiasing/taa.glsl"
#endif

//Program//
void main() {
    vec3 color = texelFetch(colortex3, texelCoord, 0).rgb;
    vec3 temp = vec3(0.0);
    float depth;

	#ifdef TEMPORAL_FILTER
		depth = texelFetch(depthtex1, texelCoord, 0).r;
	#endif

    #ifdef TAA
        DoTAA(color, temp, depth);
    #endif

    /*DRAWBUFFERS:32*/
	gl_FragData[0] = vec4(color, 1.0);
    gl_FragData[1] = vec4(temp, 1.0);
    
	#ifdef TEMPORAL_FILTER
        /*DRAWBUFFERS:326*/
        gl_FragData[2] = vec4(depth, 0.0, 0.0, 1.0);
	#endif
}

#endif

//////////Vertex Shader//////////Vertex Shader//////////Vertex Shader//////////
#ifdef VERTEX_SHADER

noperspective out vec2 texCoord;

//Uniforms//

//Attributes//

//Common Variables//

//Common Functions//

//Includes//

//Program//
void main() {
	gl_Position = ftransform();

	texCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}

#endif
