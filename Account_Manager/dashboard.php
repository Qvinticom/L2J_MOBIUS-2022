<?php
error_reporting(0);
include 'includes/config.php';

if(empty($_SESSION['account']))
{
	header('Location: index.php');
}

$error = "";
	if(isset($_POST['changePassword'])) 
	{
		$conn = new mysqli($server_host, $db_user_name, $db_user_password, $db_database);
		// Check connection
		if (mysqli_connect_errno())
		{
			$error = "Can't Connect to MySQL <h5>". mysqli_connect_error()."</h5>";
			echo "Failed to connect to MySQL: " . mysqli_connect_error();
			exit();
		}
		  
		$account = mysqli_real_escape_string($conn, $_SESSION['account']);
		$password = base64_encode(sha1($_POST['password'], true));
		$passwordOld = base64_encode(sha1($_POST['passwordOld'], true));
		
		if($_POST['password']!=$_POST['passwordVerify']){
			$error .= "Password does not match.<br>"; 
		}
		if(mb_strlen($_POST['passwordOld'])<4 || mb_strlen($_POST['passwordOld'])>16){
			$error .= "Old Password length must be 4 to 16 characters long.";
		}	
			
		if(mb_strlen($_POST['password'])<4 || mb_strlen($_POST['password'])>16){
			$error .= "Password length must be 4 to 16 characters long.";
		}	
		if($password == '')
			$error = 'Enter password';
		
		if($passwordOld == '')
			$error = 'Enter old password';
		
		$sql = "SELECT * FROM `accounts` WHERE `login`='".$account."'";
		$result = $conn->query($sql);

			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) 
				{
					
					if ($passwordOld == $row['password']) 
						{
							$error = "";
						}
					else
						{
							$error = 'Incorrect Old password';
						}
				}
			} 
			else 
			{
				$error = 'Something went wrong [1]';
			}
		if(empty($error)){
			$sqlupdate = "UPDATE `accounts` SET `password`='".$password."' WHERE (`login`='".$account."')";
			if ($conn->query($sqlupdate) === TRUE) {
				$error = "Password Successfuly Updated";
				$_SESSION['password'] = $password;
				header( "refresh:2;url=dashboard.php" );
			} 
			else {
				$error = "Something went wrong [2]";
			}
		}
		
		$conn->close();
	}
?>

<!DOCTYPE html>

<html lang="en">
<head>
	<meta content="text/html; charset=utf-8" http-equiv="content-type">
	<meta charset="utf-8">
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<meta content="" name="description">
	<meta content="" name="keywords">
	<script src="js/jquery.min.js" type="text/javascript">
	</script>

	<title>Mobius Underground - Dashboard</title>
	<link href="css/style.css" rel="stylesheet">
	<link href="images/loader.png" rel="icon" type="image/png">
	<script src='https://www.google.com/recaptcha/api.js'>
	</script>
	<link href='https://fonts.googleapis.com/css?family=Lato:300,400,500' rel='stylesheet' type='text/css'>
	<script src="js/bootstrap.min.js">
	</script>
	<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
	<script language="javascript">
	                   $(document).ready(function(){


	                    $('#changePassword').submit(function() {
	                      
	                    if($('#password').val() != $('#passwordVerify').val()){
	                       alert("Please re-enter confirm password");
	                       $('#passwordVerify').val('');
	                       $('#passwordVerify').focus();
	                       return false;
	                    }

	                        function clear_form()
	                        {
	                           $("#password").val('');
							   $("#passwordOld").val('');
	                           $("#passwordVerify").val('');
	                        }
	                   });
	                });
	</script>
</head>

<body>
	<div id="preloader">
		<div id="preloader-image">
		</div>
	</div>


	<div class="p-anim">
	</div>
	<video playsinline autoplay muted loop id="bgvid">
		<source src="media/bg.webm" type="video/webm">
		<source src="media/bg.mp4" type="video/mp4">
	</video>

	<header>
		<div class="container">
			<div class="header-left">
			</div>

			<div class="header-right">
				<div class="logo">
				</div>


				<div class="menu">
					<a href="index.php">MAIN</a> <a href="download.php">DOWNLOAD</a> <a href="#">DONATE</a> <a href="<?php echo $forum;?>">FORUM</a>
				</div>
				<br>
				<div class="entercp">
					Welcome <?php echo $_SESSION['account']; ?>  <a href="logout.php">X</a>
				</div>

				<div class="statuses">
					<div align="left" class="info">
						<div class="message">
						<?php
						// Create connection
						$conn = new mysqli($server_host, $db_user_name, $db_user_password, $db_database);
						// Check connection
						if ($conn->connect_error) {
							die("Connection failed: " . $conn->connect_error);
						} 
						$account = mysqli_real_escape_string($conn, $_SESSION['account']);
						$sql = "SELECT * FROM `accounts` WHERE `login`='".$account."'";
						$result = $conn->query($sql);

						if ($result->num_rows > 0) {
							// output data of each row
							while($row = $result->fetch_assoc()) {
								$mil = $row["lastactive"];
								$seconds = $mil / 1000;
								$date =  date("d/m/Y H:i:s", $seconds);
								echo "	<p>Last Login: " . $date. "</p>
										<p>Email: " . $row["email"]. "</p>
										<p>Created : " . $row["created_time"]. "</p>
										<p>Last IP : " . $row["lastIP"]. "</p>";
							}
						} else {
							echo "0 results";
						}
						$conn->close();
						?>
						</div>
					</div>
					<br>
					<div class="register">
						<a data-target="#modalChangePassword" data-toggle="modal" type="button">Change Password</a>
					</div>
					<div class="register">
						<a href="logout.php" type="button">Logout</a>
					</div>
					<div class="messages">
							<h4><font color="#FFFFFF"><?php
								echo (!empty($error)?"<label><strong>".$error."</strong></label>":'');
								?></font>
							</h4>
						</div>
				</div>
			</div>
		</div>
	</header>
	<div class="modal fade" id="modalChangePassword" role="dialog">
		<div class="container">
			<br>
			<!-- Modal content-->


			<div class="form">
				<div class="modal-body">
					<div class="modal-header">
						<button class="close" data-dismiss="modal" type="button">&times;</button>

						<h4 class="modal-title">Change Password</h4>
					</div>


					<div>
						<form id="changePassword" method="post">
							<div class="form-group">
								<input class="form-control" data-error="Old Password is required." id="passwordOld" name="passwordOld" placeholder="Please enter your Old Password" required="required" type="password" value="<?php if(isset($_POST['passwordOld'])) echo $_POST['passwordOld'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>
						

							<div class="form-group">
								<input class="form-control" data-error="Password is required." id="password" name="password" placeholder="Please enter your New Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Verify Password is required." id="passwordVerify" name="passwordVerify" placeholder="Please re-enter your New Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>
							<input class="form-btn btn" id="submit" name="changePassword" type="submit" value="Change Password">
						</form>
						
					</div>


					<div class="modal-footer">
						<div class="messages">
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>


	<div class="footer">
		<a href="http://l2jmobius.org"><img alt="" src="images/l2jmobius.png" title=""></a>
	</div>
	<script>
	               var url = 'dashboard.php';
	</script> 
	<script src="js/jquery.cookie.min.js">
	</script> 
	<script src="js/scripts.js">
	</script> 
	<script src="js/validator.js">
	</script> 
	<script src="js/register.js">
	</script>
<!--[if lt IE 9]>
<script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
<script src="//oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
<![endif]-->
</body>
</html>
