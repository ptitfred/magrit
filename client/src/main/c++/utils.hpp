/**
 * Copyright 2011 Frederic Menou
 * Copyright 2012 Daniel Perez
 *
 * This file is part of Magrit.
 *
 * Magrit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Magrit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Magrit.
 * If not, see <http://www.gnu.org/licenses/>.
 */
#ifndef __MAGRIT_UTILS__
#define __MAGRIT_UTILS__
/////////////////////////////////////////////////////////////////////////
// STD
#include <functional>
#include <limits>
#include <string>
#include <iostream>
#include <vector>
/////////////////////////////////////////////////////////////////////////
// BOOST
#define BOOST_FILESYSTEM_VERSION 2
#include "boost/process.hpp"
/////////////////////////////////////////////////////////////////////////

#define GCC_VERSION (__GNUC__ * 10000 \
    + __GNUC_MINOR__ * 100 \
    + __GNUC_PATCHLEVEL__)

/** Some C++11 stuff is only available in gcc 4.7 */
#if GCC_VERSION > 40700
  template <typename T>
  using sh_ptr = std::shared_pt<T>;
#else
  #define override 
  #define sh_ptr std::shared_ptr
#endif

/** Some C++11 stuff is only available in gcc 4.6 */
#if GCC_VERSION > 40600
#else
  #define nullptr NULL
#endif

std::string clear ();

template < class CharSeq >
std::string colorize ( const char* num, const CharSeq& msg, bool color=true )
{
  if ( color )
  {
    return
      std::string ( "\033[" ) +
      std::string ( num ) +
      std::string ( "m" ) +
      msg +
      clear();
  }
  else
  {
    return std::string()+msg;
  }
}

template < class CharSeq >
std::string error ( const CharSeq& msg, bool color=true )
{
  return colorize ( "91", msg, color );
}

template < class CharSeq >
std::string warning ( const CharSeq& msg, bool color=true )
{
  return colorize ( "36", msg, color );
}

template < class CharSeq >
std::string running ( const CharSeq& msg, bool color=true )
{
  return colorize ( "1;33", msg, color );
}

template < class CharSeq >
std::string pending ( const CharSeq& msg, bool color=true )
{
  return colorize ( "1;34", msg, color );
}

template < class CharSeq >
std::string cool ( const CharSeq& msg, bool color=true )
{
  return colorize ( "92", msg, color );
}

/**
 * Specific case of string concat. I didn't get it to work in the
 * general case.
 */
template <typename T, typename InputIterator>
std::string
join
( 
  const T&      separator,
  InputIterator begin_input,
  InputIterator end_input
)
{ 
  std::string output;

  if ( begin_input != end_input )
  {
    output += *begin_input;

    if ( ++begin_input != end_input )
    {
      output += separator;
    }

    output += join ( separator, begin_input, end_input);
  }

  return output;
} 

/**
 * Joins the input iterator starting from begin_input and ending at end_input
 * using separator to separate the elements in the input container.
 * The result is written to output iterator. 
 * 
 * A function can be passed to transform the input before writing it to the
 * output iterator.
 * 
 * @param separator Element to use to separate input.
 * @param begin_input Container's first element iterator.
 * @param end_input Container's end element iterator.
 * @param output Output iterator (the result is written starting
 *        by here).
 * @param func Applies this function to each element before writing
 *        it to the output iterator.       
 * @return Iterator pointing to the position after the last written.
 */
template <typename T, typename InputIterator, typename OutputIterator>
OutputIterator
join
( 
  const T&       separator,
  InputIterator  begin_input,
  InputIterator  end_input,
  OutputIterator output,
  std::function<T(typename InputIterator::value_type)> func
)
{ 
  while ( begin_input != end_input )
  {
    *output++ = func(*begin_input++);

    if ( begin_input != end_input )
    {
      *output++ = separator;
    }
  }
  
  return output; 
} 

/**
 * See the previous one. This is just a convenient method to
 * use a container instead of iterators. 
 */
template <typename T, typename Container, typename OutputIterator>
OutputIterator
join
( 
  const T&       separator,
  Container      container,
  OutputIterator output,
  std::function<T(typename Container::value_type)> func
)   
{
  typename Container::const_iterator begin_input = container.begin();
  typename Container::const_iterator end_input = container.end();

  return join<T,typename Container::const_iterator,OutputIterator>
           ( separator, begin_input, end_input, output, func );
} 


/**
 * Joins the input iterator starting from begin_input and ending at end_input
 * using separator as string to separate the elements in the input container.
 * The result is written to output iterator.
 * 
 * @param separator Element to use to separate input.
 * @param begin_input Container's first element iterator.
 * @param end_input Container's end element iterator.
 * @param output Output iterator (the result is written starting
 *        by here).
 * @return Iterator pointing to the position after the last written.
 */
template <typename T, typename InputIterator, typename OutputIterator>
OutputIterator
join
(
  const T&       separator,
  InputIterator  begin_input,
  InputIterator  end_input,
  OutputIterator output
)
{
  return join<T,InputIterator,OutputIterator>
  (
    separator,
    begin_input,
    end_input,
    output,
    [](const typename InputIterator::value_type& elem)
      -> const typename InputIterator::value_type& 
    {
      return elem;
    }
  );
}

/**
 * Splits the given string using the given delimiter.
 */
std::vector < std::string > split ( const std::string& input, char delimiter );

/**
 * Clears the console.
 *
 * @todo Make this portable.
 */
void clear_screen ();

namespace magrit
{

  struct pipeline_error : public std::runtime_error
  {
    pipeline_error ( const std::vector < std::string >& pipeline )
      : std::runtime_error
        ( 
          std::string("pipe exited abnormally: \n    -> ") +
          join ( "\n    -> ", pipeline.begin(), pipeline.end() )
        )
    {
    }
  };

  /**
   * Executes the given command with the given arguments and 
   * reads a single line of output.
   */
  std::string read_one_output_line
  ( const char* cmd, const std::vector < std::string >& args );

  /**
   * Returns the magrit.repo config variable.
   */
  std::string get_repo_remote_name ();

  /**
   * Gets the url of the git repository.
   */
  std::string get_repo_url ();

  /**
   * Name of the repository.
   */
  std::string get_repo_name ();

  /**
   * Gets the host of the git repository .
   */
  std::string get_repo_host ();

  /**
   * Gets the port of the git repository .
   */
  int get_magrit_port ();

  /**
   * Gets the user of the git repository .
   */
  std::string get_repo_user ();

  /**
   * Gets the maximum width from Git's options. 
   */
  int get_message_max_width ();

  /**
   * Returns an string no longer than width and with ellipsis if
   * it was longer. 
   */
  std::string cut_message ( const std::string& msg, size_t width );

  /**
   * Launches the given command line. 
   */
  int start_process
  (
    const std::string& program,
    const std::vector< std::string >& arguments,
    boost::process::stream_behavior _stdin,
    boost::process::stream_behavior _stdout,
    boost::process::stream_behavior _stderr,
    std::function<void (std::string&)> line_processor,
    bool _throw = true
  );

  /**
   * Launches the given pipeline.
   */
  boost::process::children start_pipeline
  ( const std::vector < boost::process::pipeline_entry >& pipeline )
  throw ( pipeline_error );

  /**
   * Creates a pipeline process with the given arguments. 
   */
  boost::process::pipeline_entry create_pipeline_member
  (
    const std::string& program,
    const std::vector< std::string >& arguments,
    boost::process::stream_behavior _stdin,
    boost::process::stream_behavior _stdout,
    boost::process::stream_behavior _stderr
  );

  /**
   * Returns a pipeline process printing to stdout the
   * sha1 signatures with `git log` according to the
   * given arguments.
   */
  boost::process::pipeline_entry get_commits_pipeline
    ( const std::vector< std::string >& arguments );

  /**
   * Returns the sha1 signatures by using `git log` with the
   * given git_args arguments.
   */
  std::vector<std::string> get_commits
    ( const std::vector<std::string>& git_args );
}
#endif
