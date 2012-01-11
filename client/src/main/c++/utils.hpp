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
/////////////////////////////////////////////////////////////////////////
// STD
#include <functional>
#include <memory>
/////////////////////////////////////////////////////////////////////////

#define sh_ptr std::shared_ptr

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
    end_input,output,
    [](const T& elem) -> const T& 
    {
      return elem;
    }
  );
}

/**
 * Joins the first command to the vector of commands.
 * The result is written as a char* array passed as
 * input (warning: the scope of command_line is the same
 * as command and command_args).
 */
/*
static void join
(
  char* command,
  char** command_args,
  uint command_args_length,
  char** command_line
) 
{
  command_line[0] = command; 

  for ( uint i = 0; i < command_args_length; ++i )
  {
    command_line[i+1] = command_args[i]; 
  }
}
*/

