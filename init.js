configuration = {
	"_id": "todoapp-replica-set",
	"members": [
		{
			"_id": 0,
			"host": "mongo-primary:27017",
		},
		{
			"_id": 1,
			"host": "mongo-secondary:27017"
		}
	]
}

rs.initiate(configuration)

/* Force primary and secondary role for rare cases when it doesn't
 * happen by default: rs.reconfig() is executed only if mongo-secondary
 * has the PRIMARY role and needs to reconfigure the replica set */
replica_configuration = rs.conf()
replica_configuration.members[0].priority = 1
replica_configuration.members[1].priority = 0.5

rs.reconfig(replica_configuration)