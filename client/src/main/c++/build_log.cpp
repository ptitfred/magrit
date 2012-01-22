/**
 * Copyright 2011 Frederic Menou
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
// MAGRIT 
#include "build_log.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::log::log ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand ),
    _log_options ("Log options"),
    _positional_parameters_desc
    ("Positional options (can be added to the end of argument list without the dashed string)")
{
  _log_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _log_options );

  _positional_parameters.add("git-args", -1);

  _positional_parameters_desc.add_options()
    ("git-args", boost::program_options::value<std::vector<std::string>>(),
     "git options");

  get_options().add ( _positional_parameters_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::log::get_name() const
{
  return "log"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::log::get_description() const
{
  return "<description to be written>";
}

/////////////////////////////////////////////////////////////////////////
const boost::program_options::positional_options_description&
magrit::log::get_positional_options () const
{
  return _positional_parameters;
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm
)
const
{
  generic_command::process_parsed_options ( arguments, vm );

  if ( vm.count ( "watch" ) )
  {
    clear_screen();
  }

  auto in_fd = make_fifo ( true ); 
  auto out_fd = make_fifo ( false ); 

  std::string cmd = "magrit status " + get_repo_name() + " -";

  send_ssh_command_bg ( in_fd, out_fd, cmd ); 
}

/////////////////////////////////////////////////////////////////////////
int magrit::log::make_fifo ( bool input ) const
{
  int result = -1;

  std::string pipe_name = std::string("/tmp/magrit-") +
                    boost::lexical_cast<std::string>(getpid()) +
                    std::string(input ? "in":"out" );

  result = mkfifo ( pipe_name.c_str(), S_IRUSR | S_IWUSR| S_IFIFO);

  if ( result != 0 )
  {
    throw std::runtime_error ( strerror( errno ) );
  }

  result = open (pipe_name.c_str(), O_RDWR /*input? O_WRONLY : O_RDONLY*/ );

  if ( result <= 0 )
  {
    throw std::runtime_error ( strerror( errno ) );
  }
  
  return result;
}
