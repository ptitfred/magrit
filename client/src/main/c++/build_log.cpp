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
#include "utils.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <iomanip>
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
     "[<since>..<until>] git revisions");

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
  const boost::program_options::variables_map& vm,
  bool allow_zero_arguments
)
const
{
  generic_command::process_parsed_options ( arguments, vm, true );

  std::vector< std::string > git_args;

  if ( vm.count ( "watch" ) )
  {
    clear_screen();
  }

  if ( vm.count ( "git-args" ) )
  {
    git_args = vm["git-args"].as< std::vector< std::string > >();
  }

  std::vector< std::string > sha1 = get_git_commits ( git_args );

  print_status ( sha1 );
}

/////////////////////////////////////////////////////////////////////////
void
print_status ( const std::vector< std::string >& sha1 )
{
  std::string port
    = boost::lexical_cast<std::string>( get_magrit_port() ).c_str();

  std::string conn_str
    = ( get_magrit_user() + std::string("@") + get_magrit_host() ).c_str();

  std::vector < std::string > cmd_line = 
  {
    "-x",
    "-p",
    port.c_str(),
    conn_str.c_str(),
    "magrit status" " /test/ " "-" 
  };

  boost::process::child ch
    = start_process
      (
        "ssh",
        cmd_line,
        boost::process::capture_stream(),
        boost::process::capture_stream(),
        boost::process::inherit_stream()
      );

  boost::process::postream& in = ch.get_stdin();
  
  boost::process::pistream& out = ch.get_stdout();

  std::for_each
  (
    sha1.begin(),
    sha1.end(),
    [&] (const std::string& sig)
    {
      in << sig << std::endl;
    }
  );

  std::string status; 
  uint num = 0;

  std::vector < std::string > cmd_line2 = 
  {
    "log",
    "--color=always", // TODO: make an option
    "-1",
    "--oneline",
    "-z",
    "<not set>"
  };

  while ( ++num <= sha1.size() && std::getline ( out, status ) )
  {
    cmd_line2.back() = sha1[num-1];
     
    boost::process::child git_log_status
      = start_process
        (
          "git",
          cmd_line2,
          boost::process::inherit_stream(),
          boost::process::capture_stream(),
          boost::process::inherit_stream()
        );
   
    std::string line;

    std::getline ( git_log_status.get_stdout(), line );

    std::cout 
      << std::left << std::setw (77)
      << line << " | " << status << std::endl;
  }
}


