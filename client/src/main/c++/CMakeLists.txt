cmake_minimum_required ( VERSION 2.6 )
# -------------------------------------------------------------------
# MAIN 
# -------------------------------------------------------------------
project ( magrit-client )
set ( magrit_VERSION_MAJOR 0 )
set ( magrit_VERSION_MINOR 1 )
set ( magrit_VERSION_PATCH 0 )
set ( magrit_FULL_VERSION
      "${magrit_VERSION_MAJOR}.${magrit_VERSION_MINOR}.${magrit_VERSION_PATCH}" )
# -------------------------------------------------------------------
# PACKAGING
# -------------------------------------------------------------------
include ( InstallRequiredSystemLibraries )
set ( CPACK_PACKAGE_DESCRIPTION_SUMMARY
      "Magrit distributed continuous integration" )
set ( CPACK_PACKAGE_VENDOR "magrit.com" )
set ( CPACK_PACKAGE_VERSION_MAJOR "${magrit_VERSION_MAJOR}" )
set ( CPACK_PACKAGE_VERSION_MINOR "${magrit_VERSION_MINOR}" )
set ( CPACK_PACKAGE_VERSION_PATCH "${magrit_VERSION_PATCH}" )
set ( CPACK_RESOURCE_FILE_README ${CMAKE_CURRENT_SOURCE_DIR}/README.md )
set ( CPACK_RESOURCE_FILE_LICENSE "${CMAKE_CURRENT_SOURCE_DIR}/COPYING" )
set ( CPACK_PACKAGE_FILE_NAME
      "${PROJECT_NAME}-${magrit_FULL_VERSION}" )
set ( CPACK_RPM_PACKAGE_SUMMARY "${CPACK_PACKAGE_DESCRIPTION_SUMMARY}" )
set ( CPACK_RPM_PACKAGE_DESCRIPTION "Magrit distributed continuous integration" )
set ( CPACK_RPM_PACKAGE_LICENSE "Gnu Affero General Public License" )
set ( CPACK_RPM_PACKAGE_VENDOR "magrit.com" )
set ( CPACK_RPM_PACKAGE_GROUP "Development/Tools" )
set ( CPACK_RPM_PACKAGE_REQUIRES "boost >= 1.40.0, boost-devel >= 1.40.0" )
set ( CPACK_RPM_CHANGELOG_FILE "${CMAKE_CURRENT_SOURCE_DIR}/ChangeLog" )
set ( CPACK_GENERATOR "ZIP;RPM" ) 
# -------------------------------------------------------------------
# Common stuff
# -------------------------------------------------------------------
# CMake scripts path
set ( CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_SOURCE_DIR}/conf/ )
set ( CMAKE_CXX_FLAGS
      "-std=c++0x -pedantic -Wall -Werror -Wconversion -Wextra -Wno-unused -s -I${CMAKE_CURRENT_SOURCE_DIR}" )
add_definitions ( -DMAGRIT_VERSION=${MAGRIT_VERSION} )
# -------------------------------------------------------------------
# Dependencies 
# -------------------------------------------------------------------
# * BOOST
find_package ( Boost 1.44.0 REQUIRED COMPONENTS program_options filesystem system )
if ( Boost_FOUND )
    include_directories ( ${Boost_INCLUDE_DIRS} )
endif ()
# -------------------------------------------------------------------
# Targets 
# -------------------------------------------------------------------
#  `---> libmagrit.so / magrit.dll
# -------------------------------------------------------------------
set ( lib_SOURCES generic_command.cpp utils.cpp build_log.cpp wait.cpp )
add_library ( magritlib SHARED ${lib_SOURCES} )
target_link_libraries ( magritlib ${Boost_LIBRARIES} )
set_target_properties ( magritlib PROPERTIES 
                        OUTPUT_NAME magrit 
                        VERSION ${magrit_FULL_VERSION} 
                        # INSTALL_RPATH '$$ORIGIN'
                      )
# -------------------------------------------------------------------
#  `---> magrit-build-log
# -------------------------------------------------------------------
add_executable ( magrit-build-log main.cpp )
target_link_libraries ( magrit-build-log magritlib )
set_target_properties ( magrit-build-log PROPERTIES COMPILE_DEFINITIONS "MAGRIT_BUILD_LOG" )
# -------------------------------------------------------------------
#  `---> magrit-wait-for
# -------------------------------------------------------------------
add_executable ( magrit-wait-for main.cpp )
target_link_libraries ( magrit-wait-for magritlib )
set_target_properties ( magrit-wait-for PROPERTIES COMPILE_DEFINITIONS "MAGRIT_WAIT" )
# -------------------------------------------------------------------
# Packaging (keep this last)
# -------------------------------------------------------------------
# TODO: allow RPM/DEB installations
install ( TARGETS magritlib magrit-build-log magrit-wait-for DESTINATION "." ) 
include ( CPack )


