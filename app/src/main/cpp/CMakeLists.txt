cmake_minimum_required(VERSION 3.22.1)

Project(HyperEmu)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -Wno-unused-function -Wimplicit-function-declaration")

add_subdirectory(proot)
add_subdirectory(virglrenderer)
add_subdirectory(OpenXR-SDK)
add_subdirectory(midihandler)

add_library(hyperemu SHARED
            xr/engine.c
            xr/framebuffer.c
            xr/input.c
            xr/main.c
            xr/math.c
            xr/renderer.c
            hyperemu/drawable.c
            hyperemu/gpu_image.c
            hyperemu/sysvshared_memory.c
            hyperemu/xconnector_epoll.c
            hyperemu/alsa_client.c)

target_link_libraries(hyperemu
                      log
                      android
                      jnigraphics
                      openxr_loader
                      aaudio
                      EGL
                      GLESv2
                      GLESv3)