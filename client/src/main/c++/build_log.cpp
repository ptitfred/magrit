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

  print_status ( git_args );
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::print_status ( const std::vector < std::string >& git_args )
const
{
  std::string port
    = boost::lexical_cast<std::string>( get_magrit_port() ).c_str();

  std::string conn_str
    = ( get_repo_user() + std::string("@") + get_repo_host() ).c_str();

  std::vector < boost::process::pipeline_entry > pipeline;

  std::vector < std::string > args;
  args.insert ( args.end(), "log" );
  args.insert ( args.end(), "--format=%H" );
  args.insert ( args.end(), git_args.begin(), git_args.end() );

  pipeline.push_back
  (
    create_pipeline_process
    (
      "git",
      args,
      boost::process::close_stream(),
      boost::process::close_stream(),
      boost::process::inherit_stream()
    )
  );
  
  pipeline.push_back
  (
    create_pipeline_process
    ( 
      "ssh",
      std::vector < std::string >
      {
        "-x",
        "-p",
        port,
        conn_str,
        std::string ( "magrit status /" ) +
        get_repo_name() +
        std::string ( "/ -" )
      },
      boost::process::close_stream(),
      boost::process::capture_stream(),
      boost::process::inherit_stream()
    )
  );

  boost::process::children statuses
    = start_pipeline ( pipeline );

  // pipeline output
  boost::process::pistream& out = statuses.back().get_stdout();
  start_process
  (
    "git",
     std::vector < std::string >
     {
       "log",
       "--color=always",
       "--oneline",
       "-z"
     },
     boost::process::inherit_stream(),
     boost::process::capture_stream(),
     boost::process::inherit_stream(),
     [&out]( const std::string& line )
     { 
       std::string status;
       std::getline( out, status );
       std::cout 
         << std::left << std::setw (77)
         << line << " | " << status << std::endl;
     }
  );
}


