macro ( download_mingw_boost MINGW_PREFIX )

  set ( HOME "$ENV{HOME}" )

  if ( HOME )

    set ( MINGW_BOOST_TMP_FOLDER "$ENV{HOME}/.cmake/mingw" )

  elseif ( WIN32 )

    set ( MINGW_BOOST_TMP_FOLDER "$ENV{APPDATA}/.cmake/mingw" ) 

  else ()

    message ( FATAL_ERROR "Cannot locate HOME folder" )

  endif ()

  set ( MINGW_BOOST_FILE "${MINGW_PREFIX}-boost-1.49.0.i686.tar.bz2" )

  if ( NOT EXISTS "${MINGW_BOOST_TMP_FOLDER}/${MINGW_BOOST_FILE}" ) 
    file ( 
      DOWNLOAD "https://github.com/downloads/daniperez/magrit/${MINGW_BOOST_FILE}"
      "${MINGW_BOOST_TMP_FOLDER}/${MINGW_BOOST_FILE}"
      STATUS status
      SHOW_PROGRESS )

    list( GET status 0 status_value )
    list( GET status 1 status_error )

    if ( NOT status_value EQUAL 0 )

      message ( FATAL_ERROR "Couldn't download the module: ${status_error}" )

    endif ()
  endif()

  if ( NOT EXISTS "${MINGW_BOOST_TMP_FOLDER}/${MINGW_PREFIX}" ) 
    execute_process(
      # Cmake's tar is buggy
      #COMMAND ${CMAKE_COMMAND} -E tar xzf "${MINGW_BOOST_TMP_FOLDER}/${MINGW_BOOST_FILE}"
      COMMAND tar xvfj "${MINGW_BOOST_TMP_FOLDER}/${MINGW_BOOST_FILE}"
      WORKING_DIRECTORY ${MINGW_BOOST_TMP_FOLDER}
      OUTPUT_QUIET )
  endif()

endmacro()

#
# NOTE: These are distro-dependent. Add your favorite
#       distro check & config parameters here.
#
IF ( EXISTS "/etc/fedora-release" )
    SET ( MINGW_PREFIX   "i686-pc-mingw32" )
    SET ( MINGW_SYSROOT  "/usr/${MINGW_PREFIX}/sys-root/mingw/" )
    SET ( Boost_COMPILER "-gcc45" )
ELSEIF ( EXISTS "/etc/debian_version" )
    IF (CMAKE_SIZEOF_VOID_P MATCHES "8")
      SET ( MINGW_PREFIX   "x86_64-w64-mingw32"     )
    ELSE ()
      SET ( MINGW_PREFIX   "i686-w64-mingw32"     )
    ENDIF ()
    download_mingw_boost ( ${MINGW_PREFIX} )
    SET ( MINGW_SYSROOT  "/usr/${MINGW_PREFIX};${MINGW_BOOST_TMP_FOLDER}/${MINGW_PREFIX}" )
    SET ( Boost_COMPILER "-mgw46" )
    SET ( BOOST_ROOT "${MINGW_BOOST_TMP_FOLDER}/usr/i686-pc-mingw32/sys-root/mingw/" )
ELSE ()
    message ( FATAL_ERROR "Unknown host platform" )
ENDIF()



include(CMakeForceCompiler)

SET ( CMAKE_SYSTEM_NAME Windows )

ADD_DEFINITIONS ( "-D__CYGWIN__" )

SET ( CMAKE_C_COMPILER ${MINGW_PREFIX}-gcc )
SET ( CMAKE_FORCE_CXX_COMPILER ${MINGW_PREFIX}-g++ GNU )
SET ( CMAKE_RC_COMPILER ${MINGW_PREFIX}-windres )

SET ( CMAKE_FIND_ROOT_PATH ${MINGW_SYSROOT} )

SET ( CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER )
SET ( CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY )
SET ( CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY )


