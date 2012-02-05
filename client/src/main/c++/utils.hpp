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
#define BOOST_PROCESS_WINDOWS_USE_NAMED_PIPE
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
 * Uses git log to retrieve info of the current git repository. The arguments
 * are passed to git log.
 */
void
get_git_commits 
( 
  const std::vector< std::string >& git_args, 
  std::function<void(const std::string& line)> func
);

/**
 * Print the status of the given revisions.
 */
void print_status ( const std::vector< std::string >& sha1 );

/**
 * Launches the given command line. 
 */
void start_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr,
  std::function<void (const std::string&)> line_processor,
  size_t limit_num_lines = std::numeric_limits<size_t>::max()
);

/**
 * Starts a pipeline process.
 */
boost::process::pipeline_entry start_pipeline_process
(
  const std::string& program,
  const std::vector< std::string >& arguments,
  boost::process::stream_behavior _stdin,
  boost::process::stream_behavior _stdout,
  boost::process::stream_behavior _stderr
);

#endif
