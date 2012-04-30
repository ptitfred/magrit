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
#include "generic_command.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <iomanip>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::generic_command::generic_command
( 
  generic_command* previous_subcommand,
  bool allow_positional
)
  : debug(true), color(true), _options ( "Main options" ), _previous_subcommand ( previous_subcommand ),
    _allow_positional ( allow_positional )
{
  namespace bpo = boost::program_options;

  _options.add_options()
    ("help,h", "produces this help message")
    ("version,v", "version of the application")
    (
      "color,c", 
      boost::program_options::value<bool>( &color )
        ->implicit_value ( true )
        ->default_value ( true ),
      "use colored output"
    )
    (
      "debug,d", 
      boost::program_options::value<bool>( &debug )
        ->implicit_value ( true )
        ->default_value ( true ),
      "show debug messages"
    );

}

/////////////////////////////////////////////////////////////////////////
void
magrit::generic_command::run
  ( const std::vector<std::string>& arguments ) const
{
  namespace bpo = boost::program_options;

  bpo::variables_map vm;

  const std::vector<std::string>
    tail_arguments ( ++arguments.begin(), arguments.end() );

  run_impl ( tail_arguments, vm );
}

/////////////////////////////////////////////////////////////////////////
void
magrit::generic_command::run_impl
(
  const std::vector<std::string>& arguments,
  boost::program_options::variables_map& vm
) const
{
  namespace bpo = boost::program_options;

  std::vector<std::string> unrecognized_arguments;

  if ( matches ( arguments, vm, unrecognized_arguments ) )
  {
    process_parsed_options ( arguments, vm, unrecognized_arguments );

    throw success();
  }
  else
  {
    auto subcommand_str = first_command ( arguments );

    if ( subcommand_str != arguments.end() )
    {
      // Subcommand passed
      auto subcommand = get_subcommand ( *subcommand_str );

      if ( subcommand != get_subcommands().end() )
      {
        (*subcommand)->run_impl
        ( 
           remove_subcommand_first ( arguments, *subcommand_str ),
           vm 
        ); 
      }
      else
      {
        print_help();
        throw option_not_recognized
        (
          std::string("command '") +
          get_name() +
          std::string("' doesn't accept subcommand '") +
          *subcommand_str +
          std::string("'")
        );
      }
    }

    // run_impl finishes throws success() if succeeded. We 
    // let matches() throw an exception to get the exact reason 
    // why the subcommand didn't match
    print_help();
    matches ( arguments, vm, unrecognized_arguments, true ); 
  }
}

/////////////////////////////////////////////////////////////////////////
void
magrit_collect_unrecognized 
(
  const std::vector<std::string>& original_arguments,
  const boost::program_options::parsed_options& parsed,
  std::vector<std::string>& unrecognized_arguments
)
{
  unrecognized_arguments
    = boost::program_options::collect_unrecognized
      ( parsed.options, boost::program_options::include_positional );
}

/////////////////////////////////////////////////////////////////////////
bool
magrit::generic_command::matches
( 
  const std::vector<std::string>& arguments,
  boost::program_options::variables_map& vm,
  std::vector<std::string>& unrecognized_arguments,
  bool _throw
) const
{
  namespace bpo = boost::program_options;

  if ( arguments.size() == 0 ) return true;

  try
  {
    if ( ! _allow_positional )
    {
      boost::program_options::positional_options_description positional;
      positional.add ("not-allowed", -1);

      auto parsed =
        bpo::command_line_parser( arguments )
          .options ( _options )
          .positional ( positional )
          .run();

      bpo::store ( parsed, vm );

      bpo::notify ( vm );

      if ( vm.count("not-allowed") )
      {
        throw boost::program_options::error("positional not allowed");
      }
    }
    else
    {
      // We use allow_unregistered to allow positional parameters 
      // that are options, e.g.: magrit-build-tools -3. The '-3'
      // shouldn't be parsed by our command but parsed as
      // a positional argument. boost::program_options would parse
      // -3 as unrecognized option.
      auto parsed =
        bpo::command_line_parser( arguments )
          .options ( _options )
          .allow_unregistered ()
          .run();

      magrit_collect_unrecognized ( arguments, parsed, unrecognized_arguments );

      bpo::store ( parsed, vm );

      bpo::notify ( vm );
    }

    return true;
  }
  catch ( boost::program_options::error& e )
  {
    // We cannot just throw a boost::program_options exception
    // due to funny extreme cases like "foo --command bar" if
    // 'bar' were a valid subcommand for 'foo' and '--command'
    // accepted arguments. We solve that recursively: we try
    // to match the longest command line (e.g. 'foo' 'bar' commands
    // with '--command' switch), and if it doesn't, we
    // try with shorter command lines (e.g. 'foo' with
    // '--command bar' switch).
    // In any case, if _throw == true, we allow throwing.
    if ( _throw )
    {
      throw e;
    }
    else
    {
      return false;
    }
  }
}

/////////////////////////////////////////////////////////////////////////
void
magrit::generic_command::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  const std::vector<std::string>& unrecognized_arguments,
  bool allow_zero_arguments
)
const
{
  if ( vm.count("help") )
  {
    print_help ();

    throw success();
  }
  else if ( vm.count("version") )
  {
    static const char* LICENSE =
    "Copyright 2011 Frederic Menou                                    \n"
    "                                                                 \n"
    "Magrit is free software: you can redistribute it and/or modify   \n"
    "it under the terms of the GNU Affero General Public License as   \n"
    "published by the Free Software Foundation, either version 3 of   \n"
    "the License, or (at your option) any later version.              \n"
    "                                                                 \n"
    "Magrit is distributed in the hope that it will be useful,        \n"
    "but WITHOUT ANY WARRANTY; without even the implied warranty of   \n"
    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    \n"
    "GNU Affero General Public License for more details.              \n"
    "                                                                 \n"
    "You should have received a copy of the GNU Affero General Public \n"
    "License along with Magrit.                                       \n"
    "If not, see <http://www.gnu.org/licenses/>.                      \n";

    std::cout << LICENSE << std::endl;
    std::cout << "Version " << QUOTE(MAGRIT_VERSION) << std::endl;
    throw success();
  }
  else if ( !allow_zero_arguments && arguments.size() == 0 )
  {
    print_help ();

    throw success();
  }
}

/////////////////////////////////////////////////////////////////////////
std::vector<sh_ptr<magrit::generic_command>>::const_iterator
magrit::generic_command::get_subcommand ( const std::string& name ) const
{
  return std::find_if
  (
    get_subcommands().begin(),
    get_subcommands().end(),
    [&] ( sh_ptr<generic_command> cmd )
    {
      return cmd->get_name() == name;
    }
  );
}

/////////////////////////////////////////////////////////////////////////
boost::program_options::options_description&
magrit::generic_command::get_options ()
{
  return _options; 
}

/////////////////////////////////////////////////////////////////////////
const std::vector< sh_ptr<magrit::generic_command>>&
magrit::generic_command::get_subcommands() const
{
  return _subcommands;
}

/////////////////////////////////////////////////////////////////////////
void magrit::generic_command::print_help () const
{
  using namespace std;

  cout << "Use: "; print_help_command(); cout << "[<options>]";

  if ( get_subcommands().size() > 0 )
  {
    cout << " <command>";
  }

  if ( _allow_positional )
  {
    cout << " [<positional options>]";
  }

  cout << endl << endl;

  cout << " " << get_description() << endl << endl;

  if ( get_subcommands().size() > 0 )
  {
    cout << "Commands:" << endl;

    print_help_subcommands_description ();

    cout << endl;

    cout << "For subcommands help, ";
    cout << "call the desired subcommand with --help" << endl << endl;
  }

  cout <<  _options;
}
/////////////////////////////////////////////////////////////////////////
void magrit::generic_command::print_help_command () const
{


  if ( _previous_subcommand != nullptr )
  {
    _previous_subcommand->print_help_command();
  }

  std::cout << get_name() << " ";
}

/////////////////////////////////////////////////////////////////////////
void magrit::generic_command::print_help_subcommands_description () const
{
  std::for_each
  (
    get_subcommands().begin(),
    get_subcommands().end(),
    [] ( sh_ptr<generic_command> cmd )
    {
      std::cout << "  " << std::setw (8) << cmd->get_name() 
                << "   " << cmd->get_description() << std::endl;
    }
  );
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string> magrit::generic_command::remove_subcommand_first
  ( const std::vector<std::string>& arguments, const std::string& arg ) const
{
  std::vector<std::string> output;

  auto to_remove =
    std::find_if (
                   arguments.begin(), arguments.end(),
                   [&arg] (const std::string& current)
                   {
                     return current == arg; 
                   }
                 );

  for ( auto it  = arguments.begin();
             it != arguments.end();
             ++it ) 
  {
    if ( it != to_remove )
    {
      output.push_back (*it);
    }
  }

  return output;
}

/////////////////////////////////////////////////////////////////////////
std::vector<std::string>::const_iterator
magrit::generic_command::first_command ( const std::vector<std::string>& arguments )
const
{
  return find_if ( 
                   arguments.begin(), arguments.end(),
                   [](const std::string& elem) -> bool
                   {
                     if ( elem[0] == '-' )
                     {
                       return false;
                     }
                     else
                     {
                       return true;
                     }
                   }
                 );
}

